package candidat.activite.infra.mailjet

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import candidat.activite.domain.EmailingCandidatsJVRService
import fr.poleemploi.perspectives.authentification.infra.autologin.AutologinService
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.emailing.domain.EmailingCandidatJVR
import fr.poleemploi.perspectives.emailing.infra.csv.ImportCandidatsJVRCSVAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapter
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, CandidatsPourBatchJVRQuery, CandidatsPourBatchJVRQueryResult}
import play.api.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MailjetEmailingCandidatsJVRService(actorSystem: ActorSystem,
                                         baseUrl: String,
                                         importFileAdapter: ImportCandidatsJVRCSVAdapter,
                                         candidatQueryHandler: CandidatQueryHandler,
                                         autologinService: AutologinService,
                                         mailjetWSAdapter: MailjetWSAdapter) extends EmailingCandidatsJVRService with Logging {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  override def envoyerEmailsCandidatsJVR: Future[Stream[CandidatId]] =
    for {
      candidatsJVR <- importFileAdapter.importerCandidats
      candidatsPourBatchJVRQueryResult <- candidatQueryHandler.handle(CandidatsPourBatchJVRQuery(candidatsJVR))
      emailingCandidatsJVR = buildEmailingCandidatsJVR(candidatsPourBatchJVRQueryResult)
      _ <- mailjetWSAdapter.envoyerCandidatsJVR(baseUrl, emailingCandidatsJVR)
    } yield candidatsJVR

  private def buildEmailingCandidatsJVR(queryResult: CandidatsPourBatchJVRQueryResult): Seq[EmailingCandidatJVR] =
    queryResult.candidats.map(c => EmailingCandidatJVR(
      email = c.email,
      autologinToken = autologinService.genererTokenCandidat(c.candidatId, c.nom, c.prenom, c.email)
    ))

}
