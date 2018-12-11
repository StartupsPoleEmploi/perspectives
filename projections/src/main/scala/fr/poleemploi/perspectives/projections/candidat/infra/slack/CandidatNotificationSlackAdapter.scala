package fr.poleemploi.perspectives.projections.candidat.infra.slack

import fr.poleemploi.perspectives.candidat.CandidatInscritEvent
import fr.poleemploi.perspectives.commun.infra.Environnement
import fr.poleemploi.perspectives.projections.candidat.CandidatNotificationProjection
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class SlackCandidatConfig(webhookURL: String,
                               environnement: Environnement)

class CandidatNotificationSlackAdapter(config: SlackCandidatConfig,
                                       wsClient: WSClient) extends CandidatNotificationProjection {

  override def onCandidatInscritEvent(event: CandidatInscritEvent): Future[Unit] =
    wsClient
      .url(s"${config.webhookURL}")
      .addHttpHeaders("Content-Type" -> "application/json")
      .post(Json.stringify(Json.obj("text" -> s"Nouveau candidat inscrit en ${config.environnement.value}")))
      .map(response => {
        if (response.status >= 400) {
          throw new RuntimeException(s"Erreur lors de l'appel à la notification slack. Code: ${response.status}. Reponse : ${response.body}")
        } else if (response.status != 200) {
          throw new RuntimeException(s"Statut non géré lors de l'appel à la notification slack. Code: ${response.status}. Reponse : ${response.body}")
        }
      })
}
