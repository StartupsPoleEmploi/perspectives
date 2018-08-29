package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat.{CVAjouteEvent, CVRemplaceEvent, CandidatInscritEvent}
import fr.poleemploi.perspectives.emailing.domain.{CandidatInscrit, EmailingService, MiseAJourCVCandidat}

import scala.concurrent.Future

class CandidatEmailProjection(emailingService: EmailingService) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[CandidatInscritEvent], classOf[CVAjouteEvent], classOf[CVRemplaceEvent])

  override def onEvent: ReceiveEvent = {
    case e: CandidatInscritEvent => onCandidatInscritEvent(e)
    case e: CVAjouteEvent => onCVAjouteEvent(e)
    case e: CVRemplaceEvent => onCVRemplaceEvent(e)
  }

  override def isReplayable: Boolean = false

  private def onCandidatInscritEvent(event: CandidatInscritEvent): Future[Unit] =
    emailingService.ajouterCandidatInscrit(CandidatInscrit(
      candidatId = event.candidatId,
      nom = event.nom,
      prenom = event.prenom,
      email = event.email,
      genre = event.genre,
      cv = false
    ))

  private def onCVAjouteEvent(event: CVAjouteEvent): Future[Unit] =
    emailingService.mettreAJourCVCandidat(MiseAJourCVCandidat(
      candidatId = event.candidatId,
      possedeCV = true
    ))

  private def onCVRemplaceEvent(event: CVRemplaceEvent): Future[Unit] =
    emailingService.mettreAJourCVCandidat(MiseAJourCVCandidat(
      candidatId = event.candidatId,
      possedeCV = true
    ))

}
