package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.nio.file.Path

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Sink}
import fr.poleemploi.perspectives.candidat.mrs.domain.{ImportMRSCandidat, MRSValideeCandidat}
import fr.poleemploi.perspectives.commun.infra.file.ImportFileAdapter
import fr.poleemploi.perspectives.commun.infra.peconnect.sql.PEConnectSqlAdapter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImportMRSCandidatPEConnect(override val config: ImportMRSCandidatPEConnectConfig,
                                 actorSystem: ActorSystem,
                                 mrsValideesCandidatsCSVAdapter: MRSValideesCandidatsCSVAdapter,
                                 mrsValideesCandidatsSqlAdapter: MRSValideesCandidatsSqlAdapter,
                                 peConnectSqlAdapter: PEConnectSqlAdapter) extends ImportMRSCandidat with ImportFileAdapter[MRSValideeCandidatPEConnect] {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  /** Intègre les full et les delta */
  val pattern: String = "DE_MRS_VALIDES_*.csv"

  override def integrerMRSValidees: Future[Stream[MRSValideeCandidat]] =
    for {
      streamMRSValideesCandidatsPEConnect <- integrerFichiers
      streamCandidatsPEConnect <-
        if (streamMRSValideesCandidatsPEConnect.isEmpty)
          Future.successful(Stream.empty)
        else
          peConnectSqlAdapter.streamCandidats.runWith(Sink.collection)
    } yield
      streamMRSValideesCandidatsPEConnect.flatMap(mrsValideeCandidatPEConnect =>
        streamCandidatsPEConnect.find(c => c.peConnectId == mrsValideeCandidatPEConnect.peConnectId).map(c =>
          MRSValideeCandidat(
            candidatId = c.candidatId,
            codeROME = mrsValideeCandidatPEConnect.codeROME,
            codeDepartement = mrsValideeCandidatPEConnect.codeDepartement,
            dateEvaluation = mrsValideeCandidatPEConnect.dateEvaluation
          )
        ))

  override def integrerFichier(fichier: Path): Future[Stream[MRSValideeCandidatPEConnect]] =
    for {
      mrsValideesCandidatPEConnect <- mrsValideesCandidatsCSVAdapter.load(FileIO.fromPath(fichier))
      mrsValideesIntegrees <- mrsValideesCandidatsSqlAdapter.ajouter(mrsValideesCandidatPEConnect)
    } yield mrsValideesIntegrees
}
