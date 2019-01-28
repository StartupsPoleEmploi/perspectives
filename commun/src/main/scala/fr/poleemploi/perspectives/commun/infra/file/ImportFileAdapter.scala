package fr.poleemploi.perspectives.commun.infra.file

import java.nio.file.{DirectoryStream, Files, Path, StandardCopyOption}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ImportFileAdapter[T] {

  def config: ImportFileAdapterConfig

  def pattern: String

  /**
    * Intègre les fichiers présent dans un répertoire. <br />
    * Best-effort : si une erreur survient lors de l'intégration d'un fichier, on continue pour les autres fichiers présent dans le répertoire
    *
    * @return
    */
  final def integrerFichiers: Future[Stream[T]] = {
    if (config.importDirectory == null || !config.importDirectory.toFile.exists()) {
      return Future.failed(new IllegalArgumentException(s"Le répertoire d'import ${config.importDirectory} n'existe pas"))
    }
    if (config.archiveDirectory == null || !config.archiveDirectory.toFile.exists()) {
      return Future.failed(new IllegalArgumentException(s"Le répertoire d'archive ${config.archiveDirectory} n'existe pas"))
    }
    val stream: DirectoryStream[Path] = Files.newDirectoryStream(config.importDirectory, pattern)
    val fichiers = stream.asScala.toList
    stream.close()
    for {
      streamHabiletes <- Future.sequence(
        fichiers.map(f =>
          integrerFichier(f).map { s =>
            Files.move(f, config.archiveDirectory.resolve(f.getFileName), StandardCopyOption.REPLACE_EXISTING)
            if (importFileLogger.isInfoEnabled()) {
              importFileLogger.info(s"${s.size} éléments intégrés dans le fichier $f")
            }
            s
          }.recover {
            case t: Throwable =>
              importFileLogger.error(s"Erreur lors de l'intégration du fichier $f", t)
              Stream.empty
          })
      ).map(_.foldLeft(Stream.empty[T])((acc, s) => acc ++ s))
    } yield streamHabiletes
  }

  def integrerFichier(fichier: Path): Future[Stream[T]]
}


