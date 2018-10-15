package fr.poleemploi.perspectives.projections.recruteur

import fr.poleemploi.cqrs.projection.QueryHandler
import fr.poleemploi.perspectives.projections.recruteur.alerte.{AlerteDto, AlerteRecruteurProjection}
import fr.poleemploi.perspectives.recruteur.{RecruteurId, TypeRecruteur}

import scala.concurrent.Future

class RecruteurQueryHandler(recruteurProjection: RecruteurProjection,
                            alerteRecruteurProjection: AlerteRecruteurProjection) extends QueryHandler {

  def typeRecruteur(recruteurId: RecruteurId): Future[Option[TypeRecruteur]] =
    recruteurProjection.typeRecruteur(recruteurId)

  def profilRecruteur(query: ProfilRecruteurQuery): Future[ProfilRecruteurDto] =
    recruteurProjection.profilRecruteur(query)

  def listerPourConseiller(query: RecruteursPourConseillerQuery): Future[RecruteursPourConseillerQueryResult] =
    recruteurProjection.listerPourConseiller(query)

  def alertesParRecruteur(query: AlertesRecruteurQuery): Future[List[AlerteDto]] =
    alerteRecruteurProjection.alertesParRecruteur(query)
}
