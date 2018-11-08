package fr.poleemploi.perspectives.candidat.mrs.infra.csv

import java.nio.file._

import akka.stream.scaladsl.FileIO
import fr.poleemploi.perspectives.candidat.mrs.domain.{HabiletesMRS, ImportHabiletesMRS}
import fr.poleemploi.perspectives.candidat.mrs.infra.importHabiletesMRSLogger
import fr.poleemploi.perspectives.candidat.mrs.infra.sql.ReferentielHabiletesMRSSqlAdapter
import fr.poleemploi.perspectives.commun.domain.CodeDepartement

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImportHabiletesMRSCsvAdapter(habiletesMRSCsvAdapter: HabiletesMRSCsvAdapter,
                                   referentielHabiletesMRSSqlAdapter: ReferentielHabiletesMRSSqlAdapter,
                                   config: ImportHabiletesMRSCsvAdapterConfig) extends ImportHabiletesMRS {

  val pattern: String = "Habiletes_*.csv"
  val importDirectory: Path = config.importDirectory
  val archiveDirectory: Path = config.archiveDirectory

  override def integrerHabiletesMRS: Future[Stream[HabiletesMRS]] = {
    if (!importDirectory.toFile.exists()) {
      return Future.failed(new IllegalArgumentException(s"Le répertoire d'import $importDirectory n'existe pas"))
    }
    if (!archiveDirectory.toFile.exists()) {
      return Future.failed(new IllegalArgumentException(s"Le répertoire d'archive $archiveDirectory n'existe pas"))
    }
    val stream: DirectoryStream[Path] = Files.newDirectoryStream(importDirectory, pattern)
    val fichiers = stream.asScala.toList
    stream.close()
    for {
      streamHabiletes <- Future.sequence(
        fichiers.map(f => integrerFichier(f))
      ).map(_.foldLeft(Stream.empty[HabiletesMRS])((acc, s) => acc ++ s))
    } yield streamHabiletes
  }

  private def integrerFichier(fichier: Path): Future[Stream[HabiletesMRS]] = {
    for {
      habiletes <- habiletesMRSCsvAdapter.load(
        source = FileIO.fromPath(fichier),
        codeDepartement = CodeDepartement(
          fichier.toFile.getName
            .replaceAll("Habiletes_", "")
            .replaceAll(".csv", "")
        )
      )
      nbHabiletesIntegrees <- referentielHabiletesMRSSqlAdapter.ajouter(habiletes)
    } yield {
      Files.move(fichier, archiveDirectory.resolve(fichier.getFileName), StandardCopyOption.REPLACE_EXISTING)
      if (importHabiletesMRSLogger.isInfoEnabled()) {
        importHabiletesMRSLogger.info(s"Nombres d'habiletés intégrées : $nbHabiletesIntegrees dans le fichier $fichier")
      }
      habiletes
    }
  }
}
