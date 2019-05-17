package fr.poleemploi.perspectives.projections.recruteur.infra.slack

import fr.poleemploi.perspectives.commun.infra.Environnement
import fr.poleemploi.perspectives.commun.infra.slack.SlackConfig
import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter
import fr.poleemploi.perspectives.projections.recruteur.RecruteurNotificationProjection
import fr.poleemploi.perspectives.recruteur.RecruteurInscritEvent
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class RecruteurNotificationSlackConfig(slackConfig: SlackConfig,
                                            environnement: Environnement) {
  def webhookURL: String = slackConfig.webhookURL
}

class RecruteurNotificationSlackAdapter(config: RecruteurNotificationSlackConfig,
                                        wsClient: WSClient) extends RecruteurNotificationProjection with WSAdapter {

  override def onRecruteurInscritEvent(event: RecruteurInscritEvent): Future[Unit] =
    wsClient
      .url(s"${config.webhookURL}")
      .addHttpHeaders(jsonContentType)
      .post(Json.obj("text" -> s"Nouveau recruteur inscrit en ${config.environnement.value}"))
      .flatMap(filtreStatutReponse(_))
      .map(_ => ())
}
