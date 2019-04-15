package fr.poleemploi.perspectives.candidat.mrs.domain

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat.{AjouterMRSValideesCommand, CandidatCommandHandler, CandidatInscritEvent}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MRSValideeProcessManager(candidatCommandHandler: CandidatCommandHandler,
                               referentielMRS: ReferentielMRS) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[CandidatInscritEvent])

  override def isReplayable: Boolean = false

  override def onEvent: ReceiveEvent = {
    case e: CandidatInscritEvent => onCandidatInscritEvent(e)
  }

  private def onCandidatInscritEvent(event: CandidatInscritEvent): Future[Unit] =
    for {
      mrsValidees <- referentielMRS.mrsValidees(event.candidatId)
      _ <- candidatCommandHandler.handle(
        AjouterMRSValideesCommand(
          id = event.candidatId,
          mrsValidees = mrsValidees
        )
      )
    } yield ()
}
