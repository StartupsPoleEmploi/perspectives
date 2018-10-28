package fr.poleemploi.perspectives.candidat.mrs.domain

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat.{AjouterMRSValideesCommand, CandidatCommandHandler, CandidatInscritEvent}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MRSValideeProcessManager(candidatCommandHandler: CandidatCommandHandler,
                               referentielMRSCandidat: ReferentielMRSCandidat) extends Projection {
  /**
    * @return La liste des classes d'événement à écouter
    */
  override def listenTo: List[Class[_ <: Event]] = List(classOf[CandidatInscritEvent])

  /**
    * Indique si la projection peut être rejouée
    */
  override def isReplayable: Boolean = false

  /**
    * Appelée lors de la réception d'un événement écouté par la projection.
    *
    * @return ReceiveEvent: le Future[Unit] indiquant un side effect sur la projection
    */
  override def onEvent: ReceiveEvent = {
    case e: CandidatInscritEvent => onCandidatInscritEvent(e)
  }

  private def onCandidatInscritEvent(event: CandidatInscritEvent): Future[Unit] =
    for {
      mrsValidees <- referentielMRSCandidat.mrsValideesParCandidat(event.candidatId)
      _ <- candidatCommandHandler.handle(
        AjouterMRSValideesCommand(
          id = event.candidatId,
          mrsValidees = mrsValidees
        )
      )
    } yield ()
}
