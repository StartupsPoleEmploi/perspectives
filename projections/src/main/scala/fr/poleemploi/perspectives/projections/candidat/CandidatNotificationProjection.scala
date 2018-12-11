package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat.CandidatInscritEvent

import scala.concurrent.Future

/**
  * Traite les notifications lors des événements survenus sur les candidats.
  */
trait CandidatNotificationProjection extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[CandidatInscritEvent])

  override def isReplayable: Boolean = false

  override def onEvent: ReceiveEvent = {
    case e: CandidatInscritEvent => onCandidatInscritEvent(e)
  }

  def onCandidatInscritEvent(event: CandidatInscritEvent): Future[Unit]
}
