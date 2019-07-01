package fr.poleemploi.perspectives.projections.emailing

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat.{AdresseModifieeEvent, CVAjouteEvent, CVRemplaceEvent, CandidatInscritEvent}
import fr.poleemploi.perspectives.emailing.domain.{CandidatInscrit, EmailingService}

import scala.concurrent.Future

/**
  * Projection qui traite les envois de mails aux candidats
  */
class CandidatEmailProjection(emailingService: EmailingService) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(
    classOf[CandidatInscritEvent],
    classOf[AdresseModifieeEvent],
    classOf[CVAjouteEvent],
    classOf[CVRemplaceEvent]
  )

  override def onEvent: ReceiveEvent = {
    case e: CandidatInscritEvent => onCandidatInscritEvent(e)
    case e: CVAjouteEvent => onCVAjouteEvent(e)
    case e: CVRemplaceEvent => onCVRemplaceEvent(e)
    case e: AdresseModifieeEvent => onAdresseModifieeEvent(e)
  }

  override def isReplayable: Boolean = false

  private def onCandidatInscritEvent(event: CandidatInscritEvent): Future[Unit] =
    emailingService.ajouterCandidatInscrit(CandidatInscrit(
      candidatId = event.candidatId,
      nom = event.nom,
      prenom = event.prenom,
      email = event.email,
      genre = event.genre
    ))

  private def onCVAjouteEvent(event: CVAjouteEvent): Future[Unit] =
    emailingService.mettreAJourCVCandidat(
      candidatId = event.candidatId,
      possedeCV = true
    )

  private def onCVRemplaceEvent(event: CVRemplaceEvent): Future[Unit] =
    emailingService.mettreAJourCVCandidat(
      candidatId = event.candidatId,
      possedeCV = true
    )

  private def onAdresseModifieeEvent(event: AdresseModifieeEvent): Future[Unit] =
    emailingService.mettreAJourAdresseCandidat(
      candidatId = event.candidatId,
      adresse = event.adresse
    )

}
