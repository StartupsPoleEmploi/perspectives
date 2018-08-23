package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.domain.Genre
import fr.poleemploi.perspectives.domain.candidat.CandidatInscrisEvent
import fr.poleemploi.perspectives.projections.infra._

import scala.concurrent.Future

class CandidatEmailProjection(mailjetEmailService: MailjetEmailService) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[CandidatInscrisEvent])

  override def onEvent: ReceiveEvent = {
    case e: CandidatInscrisEvent => onCandidatInscritEvent(e)
  }

  override def isReplayable: Boolean = false

  private def onCandidatInscritEvent(event: CandidatInscrisEvent): Future[Unit] =
    mailjetEmailService.addContactInscrit(
      AddContactRequest(
        email = s"${event.email}",
        name = s"${event.nom.capitalize}  ${event.prenom.capitalize}",
        action = "addnoforce",
        proprietesContact = ProprietesContact(
          nom = event.nom.capitalize,
          prenom = event.prenom.capitalize,
          genre = event.genre.map {
            case Genre.HOMME => "M."
            case Genre.FEMME => "Mme"
            case _ => ""
          }.getOrElse("")
        )
      )
    )
}
