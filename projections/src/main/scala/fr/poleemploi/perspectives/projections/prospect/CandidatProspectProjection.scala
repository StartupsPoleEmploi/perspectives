package fr.poleemploi.perspectives.projections.prospect

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.prospect.domain.ReferentielProspectCandidat

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatProspectProjection(referentielProspectCandidat: ReferentielProspectCandidat) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(
    classOf[CandidatInscritEvent]
  )

  override def onEvent: ReceiveEvent = {
    case e: CandidatInscritEvent => onCandidatInscritEvent(e)
  }

  override def isReplayable: Boolean = false

  private def onCandidatInscritEvent(event: CandidatInscritEvent): Future[Unit] =
    referentielProspectCandidat.supprimer(event.email)
}
