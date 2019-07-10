package fr.poleemploi.perspectives.emailing.infra.mailjet

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import fr.poleemploi.perspectives.emailing.domain.{ImportProspectService, MRSValideeProspectCandidat}
import fr.poleemploi.perspectives.emailing.infra.csv.ImportMRSValideeProspectCandidatCSVAdapter
import fr.poleemploi.perspectives.emailing.infra.sql.MailjetSqlAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MailjetImportProspectService(actorSystem: ActorSystem,
                                   importFileAdapter: ImportMRSValideeProspectCandidatCSVAdapter,
                                   mailjetSQLAdapter: MailjetSqlAdapter,
                                   mailjetWSAdapter: MailjetWSAdapter) extends ImportProspectService {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  override def importerProspectsCandidats: Future[Stream[MRSValideeProspectCandidat]] =
    for {
      mrsValidees <- importFileAdapter.importerProspectsCandidats.map(_.groupBy(_.email))
      prospects <- mailjetSQLAdapter.streamCandidats
        .runFold(mrsValidees)(
          (acc, c) => acc - c.email
        ).map(_.values.flatten.toStream)
      _ <- mailjetWSAdapter.importerProspectsCandidats(prospects)
    } yield prospects
}