package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.nio.file.{DirectoryStream, Files, Path, StandardCopyOption}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Sink}
import fr.poleemploi.perspectives.authentification.infra.peconnect.sql.PEConnectSqlAdapter
import fr.poleemploi.perspectives.candidat.mrs.domain.{ImportMRSCandidat, MRSValideeCandidat}
import fr.poleemploi.perspectives.candidat.mrs.infra.importMrsCandidatLogger

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImportMRSCandidatPEConnect(config: ImportMRSCandidatPEConnectConfig,
                                 actorSystem: ActorSystem,
                                 mrsValideesCandidatsCSVAdapter: MRSValideesCandidatsCSVAdapter,
                                 mrsValideesCandidatsSqlAdapter: MRSValideesCandidatsSqlAdapter,
                                 peConnectSqlAdapter: PEConnectSqlAdapter) extends ImportMRSCandidat {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

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
      streamMRSValideesCandidatsPEConnect <- Future.sequence(fichiers.map(f =>
        integrerFichier(f).recover {
          case t: Throwable =>
            importMrsCandidatLogger.error(s"Erreur lors de l'intégration du fichier $f", t)
            Stream.empty
        })).map(_.foldLeft(Stream.empty[MRSValideeCandidatPEConnect])((acc, s) => acc ++ s))
      streamCandidatsPEConnect <-
        if (streamMRSValideesCandidatsPEConnect.isEmpty)
          Future.successful(Stream.empty)
        else
          peConnectSqlAdapter.getAllCandidats.runWith(Sink.collection)
    } yield
      streamMRSValideesCandidatsPEConnect
        .flatMap(mrsValideeCandidatPEConnect =>
          streamCandidatsPEConnect.find(c => c.peConnectId == mrsValideeCandidatPEConnect.peConnectId).map(c =>
            MRSValideeCandidat(
              candidatId = c.candidatId,
              codeROME = mrsValideeCandidatPEConnect.codeROME,
              codeDepartement = mrsValideeCandidatPEConnect.codeDepartement,
              dateEvaluation = mrsValideeCandidatPEConnect.dateEvaluation
            )
          ))
  }

  private def integrerFichier(fichier: Path): Future[Stream[MRSValideeCandidatPEConnect]] = {
    for {
      mrsValideesCandidatPEConnect <- mrsValideesCandidatsCSVAdapter.load(FileIO.fromPath(fichier))
      mrsValideesIntegrees <- mrsValideesCandidatsSqlAdapter.ajouter(mrsValideesCandidatPEConnect)
    } yield {
      Files.move(fichier, archiveDirectory.resolve(fichier.getFileName), StandardCopyOption.REPLACE_EXISTING)
      if (importMrsCandidatLogger.isInfoEnabled()) {
        importMrsCandidatLogger.info(s"Nombres de MRS validées intégrées dans le référentiel : ${mrsValideesIntegrees.size}/${mrsValideesCandidatPEConnect.size} reçues dans le fichier $fichier")
      }
      mrsValideesIntegrees
    }
  }
}
