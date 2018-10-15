package fr.poleemploi.perspectives.projections.recruteur.alerte

import akka.NotUsed
import akka.stream.scaladsl.Source
import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.projections.recruteur.AlertesRecruteurQuery
import fr.poleemploi.perspectives.projections.recruteur.alerte.infra.sql.AlerteRecruteurSqlAdapter
import fr.poleemploi.perspectives.recruteur._

import scala.concurrent.Future

class AlerteRecruteurProjection(alerteRecruteurSqlAdapter: AlerteRecruteurSqlAdapter) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[RecruteurEvent])

  override def isReplayable: Boolean = true

  override def onEvent: ReceiveEvent = {
    case e: AlerteRecruteurCreeEvent => alerteRecruteurSqlAdapter.onAlerteRecruteurCreeEvent(e)
    case e: AlerteRecruteurSupprimeeEvent => alerteRecruteurSqlAdapter.onAlerteRecruteurSupprimeeEvent(e)
    case e: ProfilGerantModifieEvent => alerteRecruteurSqlAdapter.onProfilGerantModifieEvent(e)
    case e: ProfilModifieEvent => alerteRecruteurSqlAdapter.onProfilModifieEvent(e)
    case _ => Future.successful(())
  }

  def alertesQuotidiennes: Source[AlerteRecruteurDto, NotUsed] =
    alerteRecruteurSqlAdapter.alertesQuotidiennes

  def alertesHebdomaraires: Source[AlerteRecruteurDto, NotUsed] =
    alerteRecruteurSqlAdapter.alertesHebdomaraires

  def alertesParRecruteur(query: AlertesRecruteurQuery): Future[List[AlerteDto]] =
    alerteRecruteurSqlAdapter.alertesParRecruteur(query)
}
