package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.nio.file.Path

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Sink}
import fr.poleemploi.perspectives.candidat.mrs.domain.{ImportMRS, MRSValideeCandidat}
import fr.poleemploi.perspectives.commun.infra.file.ImportFileAdapter
import fr.poleemploi.perspectives.commun.infra.peconnect.sql.PEConnectSqlAdapter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImportMRSPEConnectAdapter(override val config: ImportMRSPEConnectConfig,
                                actorSystem: ActorSystem,
                                mrsValideesCSVAdapter: MRSValideesCSVAdapter,
                                mrsValideesSqlAdapter: MRSValideesSqlAdapter,
                                peConnectSqlAdapter: PEConnectSqlAdapter) extends ImportMRS with ImportFileAdapter[MRSValideePEConnect] {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  /** Intègre les full et les delta */
  val pattern: String = "DE_MRS_VALIDES_*.csv"

  override def integrerMRSValidees: Future[Stream[MRSValideeCandidat]] =
    for {
      streamMRSValideesPEConnect <- integrerFichiers
      streamCandidatsPEConnect <-
        if (streamMRSValideesPEConnect.isEmpty)
          Future.successful(Stream.empty)
        else
          peConnectSqlAdapter.streamCandidats.runWith(Sink.collection)
    } yield
      streamMRSValideesPEConnect.flatMap(mrsValideePEConnect =>
        streamCandidatsPEConnect.find(c => c.peConnectId == mrsValideePEConnect.peConnectId).map(c =>
          MRSValideeCandidat(
            candidatId = c.candidatId,
            codeROME = mrsValideePEConnect.codeROME,
            codeDepartement = mrsValideePEConnect.codeDepartement,
            dateEvaluation = mrsValideePEConnect.dateEvaluation
          )
        ))

  override def integrerFichier(fichier: Path): Future[Stream[MRSValideePEConnect]] =
    for {
      mrsValideesPEConnect <- mrsValideesCSVAdapter.load(FileIO.fromPath(fichier))
      mrsValideesIntegrees <- mrsValideesSqlAdapter.ajouter(mrsValideesPEConnect)
    } yield mrsValideesIntegrees
}
