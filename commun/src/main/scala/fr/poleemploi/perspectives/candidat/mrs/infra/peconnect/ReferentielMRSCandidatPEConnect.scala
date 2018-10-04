package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.nio.file._

import akka.stream.scaladsl.FileIO
import fr.poleemploi.perspectives.authentification.infra.PEConnectService
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.mrs.domain.{MRSValidee, MRSValideeCandidat, ReferentielMRSCandidat}
import fr.poleemploi.perspectives.candidat.mrs.infra.csv.MRSValideesCSVAdapter
import fr.poleemploi.perspectives.candidat.mrs.infra.referentielMrsCandidatLogger
import fr.poleemploi.perspectives.candidat.mrs.infra.sql.MRSValideesSqlAdapter

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Referentiel qui intègre les MRS validées depuis un fichier CSV contenant les identifiants PEConnect vers une table dans la base de l'application perspectives.
  */
class ReferentielMRSCandidatPEConnect(config: ReferentielMRSCandidatPEConnectConfig,
                                      mrsValideesCSVLoader: MRSValideesCSVAdapter,
                                      mrsValideesPostgresSql: MRSValideesSqlAdapter,
                                      peConnectService: PEConnectService) extends ReferentielMRSCandidat {

  /** Intègre les full et les delta */
  val pattern: String = "DE_MRS_VALIDES_*.csv"
  val importDirectory: Path = config.importDirectory
  val archiveDirectory: Path = config.archiveDirectory

  override def integrerMRSValidees: Future[Stream[MRSValideeCandidat]] = {
    if (!importDirectory.toFile.exists()) {
      return Future.failed(new RuntimeException(s"Le répertoire d'import $importDirectory n'existe pas"))
    }
    if (!archiveDirectory.toFile.exists()) {
      return Future.failed(new RuntimeException(s"Le répertoire d'archive $archiveDirectory n'existe pas"))
    }
    val stream: DirectoryStream[Path] = Files.newDirectoryStream(importDirectory, pattern)
    val fichiers = stream.asScala.toList
    stream.close()
    for {
      streamCandidatPEConnect <- Future.sequence(fichiers.map(f =>
        integrerFichier(f).recover {
          case t: Throwable =>
            referentielMrsCandidatLogger.error(s"Erreur lors de l'intégration du fichier $f", t)
            Stream.empty
        })).map(_.foldLeft(Stream.empty[MRSValideeCandidatPEConnect])((acc, s) => acc ++ s))
      streamCandidat <- Future.sequence(streamCandidatPEConnect.map(mrsValideeCandidatPEConnect =>
        peConnectService.findCandidatId(mrsValideeCandidatPEConnect.peConnectId).map(_.map(candidatId => MRSValideeCandidat(
          candidatId = candidatId,
          codeROME = mrsValideeCandidatPEConnect.codeROME,
          dateEvaluation = mrsValideeCandidatPEConnect.dateEvaluation
        ))
      )))
    } yield streamCandidat.flatten
  }

  override def mrsValideesParCandidat(candidatId: CandidatId): Future[List[MRSValidee]] =
    for {
      candidatPEConnect <- peConnectService.getCandidat(candidatId)
      mrsValidees <- mrsValideesPostgresSql.metiersEvaluesParCandidat(candidatPEConnect.peConnectId)
    } yield mrsValidees

  private def integrerFichier(fichier: Path): Future[Stream[MRSValideeCandidatPEConnect]] = {
    for {
      mrsValideesCandidatPEConnect <- mrsValideesCSVLoader.load(FileIO.fromPath(fichier))
      nbMrsValideesIntegrees <- mrsValideesPostgresSql.ajouter(mrsValideesCandidatPEConnect)
    } yield {
      Files.move(fichier, archiveDirectory.resolve(fichier.getFileName), StandardCopyOption.REPLACE_EXISTING)
      if (referentielMrsCandidatLogger.isInfoEnabled()) {
        referentielMrsCandidatLogger.info(s"Nombres de MRS validées intégrées : $nbMrsValideesIntegrees dans le fichier $fichier")
      }
      mrsValideesCandidatPEConnect
    }
  }
}