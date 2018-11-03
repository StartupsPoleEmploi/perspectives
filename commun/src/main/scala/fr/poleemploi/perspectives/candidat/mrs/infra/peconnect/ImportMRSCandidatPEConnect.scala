package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.nio.file.{DirectoryStream, Files, Path, StandardCopyOption}

import akka.stream.scaladsl.FileIO
import fr.poleemploi.perspectives.authentification.infra.peconnect.sql.PEConnectSqlAdapter
import fr.poleemploi.perspectives.candidat.mrs.domain.{ImportMRSCandidat, MRSValideeCandidat}
import fr.poleemploi.perspectives.candidat.mrs.infra.importMrsCandidatLogger
import fr.poleemploi.perspectives.candidat.mrs.infra.sql.MRSValideesSqlAdapter

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImportMRSCandidatPEConnect(config: ImportMRSCandidatPEConnectConfig,
                                 mrsValideesCSVAdapter: MRSValideesCSVAdapter,
                                 mrsValideesSqlAdapter: MRSValideesSqlAdapter,
                                 peConnectSqlAdapter: PEConnectSqlAdapter) extends ImportMRSCandidat {

  /** Intègre les full et les delta */
  val pattern: String = "DE_MRS_VALIDES_*.csv"
  val importDirectory: Path = config.importDirectory
  val archiveDirectory: Path = config.archiveDirectory

  override def integrerMRSValidees: Future[Stream[MRSValideeCandidat]] = {
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
      streamCandidatPEConnect <- Future.sequence(fichiers.map(f =>
        integrerFichier(f).recover {
          case t: Throwable =>
            importMrsCandidatLogger.error(s"Erreur lors de l'intégration du fichier $f", t)
            Stream.empty
        })).map(_.foldLeft(Stream.empty[MRSValideeCandidatPEConnect])((acc, s) => acc ++ s))
      streamCandidat <- Future.sequence(streamCandidatPEConnect.map(mrsValideeCandidatPEConnect =>
        peConnectSqlAdapter.findCandidatId(mrsValideeCandidatPEConnect.peConnectId).map(_.map(candidatId => MRSValideeCandidat(
          candidatId = candidatId,
          codeROME = mrsValideeCandidatPEConnect.codeROME,
          dateEvaluation = mrsValideeCandidatPEConnect.dateEvaluation
        ))
        )))
    } yield streamCandidat.flatten
  }

  private def integrerFichier(fichier: Path): Future[Stream[MRSValideeCandidatPEConnect]] = {
    for {
      mrsValideesCandidatPEConnect <- mrsValideesCSVAdapter.load(FileIO.fromPath(fichier))
      nbMrsValideesIntegrees <- mrsValideesSqlAdapter.ajouter(mrsValideesCandidatPEConnect)
    } yield {
      Files.move(fichier, archiveDirectory.resolve(fichier.getFileName), StandardCopyOption.REPLACE_EXISTING)
      if (importMrsCandidatLogger.isInfoEnabled()) {
        importMrsCandidatLogger.info(s"Nombres de MRS validées intégrées : $nbMrsValideesIntegrees dans le fichier $fichier")
      }
      mrsValideesCandidatPEConnect
    }
  }
}
