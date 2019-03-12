package fr.poleemploi.perspectives.projections.recruteur.alerte

import akka.NotUsed
import akka.stream.scaladsl.Source
import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.projections.recruteur.alerte.infra.sql.AlerteRecruteurSqlAdapter
import fr.poleemploi.perspectives.recruteur._

import scala.concurrent.Future

class AlerteRecruteurProjection(adapter: AlerteRecruteurSqlAdapter) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[RecruteurEvent])

  override def isReplayable: Boolean = true

  override def onEvent: ReceiveEvent = {
    case e: AlerteRecruteurCreeEvent => adapter.onAlerteRecruteurCreeEvent(e)
    case e: AlerteRecruteurSupprimeeEvent => adapter.onAlerteRecruteurSupprimeeEvent(e)
    case e: ProfilGerantModifieEvent => adapter.onProfilGerantModifieEvent(e)
    case e: ProfilModifieEvent => adapter.onProfilModifieEvent(e)
    case _ => Future.successful(())
  }

  def alertesQuotidiennes: Source[AlerteRecruteurDTO, NotUsed] =
    adapter.alertesQuotidiennes

  def alertesHebdomaraires: Source[AlerteRecruteurDTO, NotUsed] =
    adapter.alertesHebdomaraires

  def alertesParRecruteur(query: AlertesRecruteurQuery): Future[AlertesRecruteurQueryResult] =
    adapter.alertesParRecruteur(query)
}
