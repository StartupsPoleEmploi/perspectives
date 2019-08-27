package fr.poleemploi.perspectives.projections.recruteur

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.projections.recruteur.infra.sql.RecruteurProjectionSqlAdapter
import fr.poleemploi.perspectives.recruteur._

class RecruteurProjection(adapter: RecruteurProjectionSqlAdapter) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[RecruteurEvent])

  override def isReplayable: Boolean = true

  override def onEvent: ReceiveEvent = {
    case e: RecruteurInscritEvent => adapter.onRecruteurInscritEvent(e)
    case e: RecruteurConnecteEvent => adapter.onRecruteurConnecteEvent(e)
    case e: ProfilModifieEvent => adapter.onProfilModifieEvent(e)
    case e: AdresseRecruteurModifieeEvent => adapter.onAdresseModifieeEvent(e)
    case e: ProfilGerantModifieEvent => adapter.onProfilGerantModifieEvent(e)
  }
}
