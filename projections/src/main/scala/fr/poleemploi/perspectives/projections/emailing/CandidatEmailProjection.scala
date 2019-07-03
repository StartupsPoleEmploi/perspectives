package fr.poleemploi.perspectives.projections.emailing

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.emailing.domain.{CandidatInscrit, EmailingService, MRSValideeCandidat}
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatEmailProjection(emailingService: EmailingService,
                              referentielMetier: ReferentielMetier) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(
    classOf[CandidatInscritEvent],
    classOf[AdresseModifieeEvent],
    classOf[MRSAjouteeEvent],
    classOf[CVAjouteEvent],
    classOf[CVRemplaceEvent]
  )

  override def onEvent: ReceiveEvent = {
    case e: CandidatInscritEvent => onCandidatInscritEvent(e)
    case e: AdresseModifieeEvent => onAdresseModifieeEvent(e)
    case e: MRSAjouteeEvent => onMRSAjouteeEvent(e)
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
      genre = event.genre
    ))

  private def onAdresseModifieeEvent(event: AdresseModifieeEvent): Future[Unit] =
    emailingService.mettreAJourAdresseCandidat(
      candidatId = event.candidatId,
      adresse = event.adresse
    )

  private def onMRSAjouteeEvent(event: MRSAjouteeEvent): Future[Unit] =
    referentielMetier.metierParCodeROME(event.codeROME).flatMap(metier =>
      emailingService.mettreAJourDerniereMRSValideeCandidat(
        candidatId = event.candidatId,
        mrsValideeCandidat = MRSValideeCandidat(
          metier = metier,
          dateEvaluation = event.dateEvaluation
        )
      )
    )

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

}
