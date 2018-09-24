package fr.poleemploi.perspectives.projections.recruteur

import fr.poleemploi.cqrs.projection.QueryHandler
import fr.poleemploi.perspectives.recruteur.{RecruteurId, TypeRecruteur}

import scala.concurrent.Future

class RecruteurQueryHandler(recruteurProjection: RecruteurProjection) extends QueryHandler {

  def typeRecruteur(recruteurId: RecruteurId): Future[Option[TypeRecruteur]] =
    recruteurProjection.typeRecruteur(recruteurId)

  def profilRecruteur(query: ProfilRecruteurQuery): Future[ProfilRecruteurDto] =
    recruteurProjection.profilRecruteur(query)

  def listerPourConseiller(query: RecruteursPourConseillerQuery): Future[List[RecruteurPourConseillerDto]] =
    recruteurProjection.listerPourConseiller(query)
}
