package fr.poleemploi.perspectives.projections.recruteur

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.emailing.domain.{EmailingService, RecruteurInscrit}
import fr.poleemploi.perspectives.recruteur.RecruteurInscritEvent

import scala.concurrent.Future

class RecruteurEmailProjection(emailingService: EmailingService) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[RecruteurInscritEvent])

  override def onEvent: ReceiveEvent = {
    case e: RecruteurInscritEvent => onRecruteurInscritEvent(e)
  }

  override def isReplayable: Boolean = false

  private def onRecruteurInscritEvent(event: RecruteurInscritEvent): Future[Unit] =
    emailingService.ajouterRecruteurInscrit(RecruteurInscrit(
      recruteurId = event.recruteurId,
      nom = event.nom,
      prenom = event.prenom,
      email = event.email,
      genre = event.genre
    ))

}