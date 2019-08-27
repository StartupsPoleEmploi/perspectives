package fr.poleemploi.perspectives.projections.recruteur

import fr.poleemploi.perspectives.projections.recruteur.infra.sql.RecruteurProjectionSqlAdapter

import scala.concurrent.Future

class RecruteurProjectionQuery(adapter: RecruteurProjectionSqlAdapter) {

  def typeRecruteur(query: TypeRecruteurQuery): Future[TypeRecruteurQueryResult] =
    adapter.typeRecruteur(query)

  def profilRecruteur(query: ProfilRecruteurQuery): Future[ProfilRecruteurQueryResult] =
    adapter.profilRecruteur(query)

  def listerPourConseiller(query: RecruteursPourConseillerQuery): Future[RecruteursPourConseillerQueryResult] =
    adapter.listerPourConseiller(query)
}
