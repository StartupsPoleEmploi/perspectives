package fr.poleemploi.perspectives.projections.emailing

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.emailing.domain.{EmailingService, RecruteurInscrit}
import fr.poleemploi.perspectives.recruteur.{ProfilModifieEvent, RecruteurInscritEvent}

import scala.concurrent.Future

class RecruteurEmailProjection(emailingService: EmailingService) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(
    classOf[RecruteurInscritEvent],
    classOf[ProfilModifieEvent]
  )

  override def onEvent: ReceiveEvent = {
    case e: RecruteurInscritEvent => onRecruteurInscritEvent(e)
    case e: ProfilModifieEvent => onProfilModifieEvent(e)
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

  private def onProfilModifieEvent(event: ProfilModifieEvent): Future[Unit] =
    emailingService.mettreAJourTypeRecruteur(
      recruteurId = event.recruteurId,
      typeRecruteur = event.typeRecruteur
    )
}