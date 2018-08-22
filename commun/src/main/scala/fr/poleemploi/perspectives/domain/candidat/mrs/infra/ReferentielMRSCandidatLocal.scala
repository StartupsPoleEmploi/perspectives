package fr.poleemploi.perspectives.domain.candidat.mrs.infra

import java.nio.file._

import akka.stream.scaladsl.FileIO
import fr.poleemploi.perspectives.domain.authentification.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.domain.candidat.mrs.{MRSValidee, ReferentielMRSCandidat}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Referentiel qui intègre les MRS validées depuis un fichier CSV vers une table dans la base de l'application perspectives.
  */
class ReferentielMRSCandidatLocal(referentielMRSCandidatConfig: ReferentielMRSCandidatConfig,
                                  mrsValideesCSVLoader: MRSValideesCSVLoader,
                                  mrsValideesPostgresSql: MRSValideesPostgreSql) extends ReferentielMRSCandidat {

  /** Intègre les full et les delta */
  val pattern: String = "DE_MRS_VALIDES_*.csv"

  override def integrerMRSValidees: Future[Unit] = {
    if (!referentielMRSCandidatConfig.importDirectory.toFile.exists()) {
      return Future.failed(new RuntimeException(s"Le répertoire d'import ${referentielMRSCandidatConfig.importDirectory} n'existe pas"))
    }
    if (!referentielMRSCandidatConfig.archiveDirectory.toFile.exists()) {
      return Future.failed(new RuntimeException(s"Le répertoire d'archive ${referentielMRSCandidatConfig.archiveDirectory} n'existe pas"))
    }
    val stream: DirectoryStream[Path] = Files.newDirectoryStream(referentielMRSCandidatConfig.importDirectory, pattern)
    val fichiers = stream.asScala.toList
    stream.close()
    Future.sequence(fichiers.map(f =>
      integrerFichier(f).recover {
        case t: Throwable =>
          referentielMrsCandidatLogger.error(s"Erreur lors de l'intégration du fichier $f", t)
    })).map(_ => ())
  }

  override def metiersValidesParCandidat(peConnectId: PEConnectId): Future[List[MRSValidee]] =
    mrsValideesPostgresSql.metiersEvaluesParCandidat(peConnectId)

  private def integrerFichier(fichier: Path): Future[Unit] = {
    for {
      datas <- mrsValideesCSVLoader.load(FileIO.fromPath(fichier))
      nbMrsValideesIntegrees <- mrsValideesPostgresSql.ajouter(datas)
    } yield {
      Files.move(fichier, referentielMRSCandidatConfig.archiveDirectory.resolve(fichier.getFileName), StandardCopyOption.REPLACE_EXISTING)
      if (referentielMrsCandidatLogger.isInfoEnabled()) {
        referentielMrsCandidatLogger.info(s"Nombres de MRS validées intégrées : $nbMrsValideesIntegrees dans le fichier $fichier")
      }
    }
  }
}