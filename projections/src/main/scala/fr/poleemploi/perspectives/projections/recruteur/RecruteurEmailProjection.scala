package fr.poleemploi.perspectives.projections.recruteur

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.domain.Genre
import fr.poleemploi.perspectives.domain.recruteur.RecruteurInscrisEvent
import fr.poleemploi.perspectives.projections.infra.{AddContactRequest, MailjetEmailService, ProprietesContact}

import scala.concurrent.Future

class RecruteurEmailProjection (mailjetEmailService: MailjetEmailService) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[RecruteurInscrisEvent])

  override def onEvent: ReceiveEvent = {
    case e: RecruteurInscrisEvent => onRecruteurInscritEvent(e)
  }

  override def isReplayable: Boolean = false

  private def onRecruteurInscritEvent(event: RecruteurInscrisEvent): Future[Unit] =
    mailjetEmailService.addRecruteurInscrit(
      AddContactRequest(
        email = s"${event.email}",
        name = s"${event.nom.capitalize}  ${event.prenom.capitalize}",
        action = "addnoforce",
        proprietesContact = ProprietesContact(
          nom = event.nom.capitalize,
          prenom = event.prenom.capitalize,
          genre = event.genre match {
            case Genre.HOMME => "M."
            case Genre.FEMME => "Mme"
            case _ => ""
          }
        )
      )
    )
}