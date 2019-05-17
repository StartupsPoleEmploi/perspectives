package fr.poleemploi.perspectives.projections.recruteur.infra.local

import fr.poleemploi.perspectives.projections.recruteur.RecruteurNotificationProjection
import fr.poleemploi.perspectives.recruteur.RecruteurInscritEvent

import scala.concurrent.Future

class RecruteurNotificationLocalAdapter extends RecruteurNotificationProjection {

  override def onRecruteurInscritEvent(event: RecruteurInscritEvent): Future[Unit] =
    Future.successful({
      println(event)
    })
}
