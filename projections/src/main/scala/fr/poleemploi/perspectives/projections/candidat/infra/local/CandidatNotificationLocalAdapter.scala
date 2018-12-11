package fr.poleemploi.perspectives.projections.candidat.infra.local

import fr.poleemploi.perspectives.candidat.CandidatInscritEvent
import fr.poleemploi.perspectives.projections.candidat.CandidatNotificationProjection

import scala.concurrent.Future

class CandidatNotificationLocalAdapter extends CandidatNotificationProjection {

  override def onCandidatInscritEvent(event: CandidatInscritEvent): Future[Unit] =
    Future.successful(())
}
