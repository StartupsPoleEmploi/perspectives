package fr.poleemploi.perspectives.projections.recruteur

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.recruteur.RecruteurInscritEvent

import scala.concurrent.Future

/**
  * Traite les notifications lors des événements survenus sur les recruteurs.
  */
trait RecruteurNotificationProjection extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[RecruteurInscritEvent])

  override def isReplayable: Boolean = false

  override def onEvent: ReceiveEvent = {
    case e: RecruteurInscritEvent => onRecruteurInscritEvent(e)
  }

  def onRecruteurInscritEvent(event: RecruteurInscritEvent): Future[Unit]
}
