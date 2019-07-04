package fr.poleemploi.perspectives.emailing.infra.mailjet

import java.nio.file.Path

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO
import fr.poleemploi.perspectives.commun.infra.file.{ImportFileAdapter, ImportFileAdapterConfig}
import fr.poleemploi.perspectives.emailing.domain.{ImportProspectService, MRSValideeProspectCandidat}
import fr.poleemploi.perspectives.emailing.infra.csv.MRSValideesProspectCandidatCSVAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MailjetImportProspectService(override val config: ImportFileAdapterConfig,
                                   actorSystem: ActorSystem,
                                   mrsValideesProspectCandidatCSVAdapter: MRSValideesProspectCandidatCSVAdapter,
                                   mailjetWSAdapter: MailjetWSAdapter) extends ImportFileAdapter[MRSValideeProspectCandidat]
  with ImportProspectService {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  override val pattern: String = "DE_MRS_VALIDES_*.csv"

  override def integrerFichier(fichier: Path): Future[Stream[MRSValideeProspectCandidat]] =
    for {
      mrsValidees <- mrsValideesProspectCandidatCSVAdapter.load(FileIO.fromPath(fichier))
      _ <- mailjetWSAdapter.importerProspectsCandidats(mrsValidees)
    } yield mrsValidees

  override def importerProspectsCandidat: Future[Stream[MRSValideeProspectCandidat]] = integrerFichiers
}