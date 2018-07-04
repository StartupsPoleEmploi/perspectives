package fr.poleemploi.perspectives.projections

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.{AggregateId, Event}
import fr.poleemploi.perspectives.domain.candidat.CandidatInscrisEvent
import fr.poleemploi.perspectives.infra.Environnement
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class SlackCandidatConfig(webhookURL: String,
                               environnement: Environnement)

class CandidatNotificationSlackProjection(slackCandidatConfig: SlackCandidatConfig,
                                          wsClient: WSClient) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[Event])

  override def isReplayable: Boolean = false

  override def onEvent(aggregateId: AggregateId): ReceiveEvent = {
    case e: CandidatInscrisEvent =>
      onCandidatInscrisEvent(
        aggregateId = aggregateId,
        event = e
      )
  }

  private def onCandidatInscrisEvent(aggregateId: AggregateId,
                                     event: CandidatInscrisEvent): Future[Unit] = {
    wsClient
      .url(s"${slackCandidatConfig.webhookURL}")
      .addHttpHeaders("Content-Type" -> "application/json")
      .post(Json.stringify(Json.obj("text" -> s"Nouveau candidat inscrit en ${slackCandidatConfig.environnement.value}")))
      .map(response => {
        if (response.status >= 400) {
          println(s"Erreur lors de l'appel à la notification slack. Code: ${response.status}. Reponse : ${response.body}")
        } else if (response.status != 200) {
          println(s"Statut non géré lors de l'appel à la notification slack. Code: ${response.status}. Reponse : ${response.body}")
        }
      })
  }
}
