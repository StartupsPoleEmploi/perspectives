package fr.poleemploi.perspectives.candidat.mrs.infra.file

import java.nio.file._

import akka.stream.scaladsl.FileIO
import fr.poleemploi.perspectives.candidat.mrs.domain.{MRSValidee, ReferentielMRSCandidat}
import fr.poleemploi.perspectives.candidat.mrs.infra.csv.MRSValideesCSVAdapter
import fr.poleemploi.perspectives.candidat.mrs.infra.referentielMrsCandidatLogger
import fr.poleemploi.perspectives.candidat.mrs.infra.sql.MRSValideesSqlAdapter
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Referentiel qui intègre les MRS validées depuis un fichier CSV vers une table dans la base de l'application perspectives.
  */
class ReferentielMRSCandidatFile(referentielMRSCandidatFileConfig: ReferentielMRSCandidatFileConfig,
                                 mrsValideesCSVLoader: MRSValideesCSVAdapter,
                                 mrsValideesPostgresSql: MRSValideesSqlAdapter) extends ReferentielMRSCandidat {

  /** Intègre les full et les delta */
  val pattern: String = "DE_MRS_VALIDES_*.csv"
  val importDirectory: Path = referentielMRSCandidatFileConfig.importDirectory
  val archiveDirectory: Path = referentielMRSCandidatFileConfig.archiveDirectory

  override def integrerMRSValidees: Future[Unit] = {
    if (!importDirectory.toFile.exists()) {
      return Future.failed(new RuntimeException(s"Le répertoire d'import $importDirectory n'existe pas"))
    }
    if (!archiveDirectory.toFile.exists()) {
      return Future.failed(new RuntimeException(s"Le répertoire d'archive $archiveDirectory n'existe pas"))
    }
    val stream: DirectoryStream[Path] = Files.newDirectoryStream(importDirectory, pattern)
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
      Files.move(fichier, archiveDirectory.resolve(fichier.getFileName), StandardCopyOption.REPLACE_EXISTING)
      if (referentielMrsCandidatLogger.isInfoEnabled()) {
        referentielMrsCandidatLogger.info(s"Nombres de MRS validées intégrées : $nbMrsValideesIntegrees dans le fichier $fichier")
      }
    }
  }
}