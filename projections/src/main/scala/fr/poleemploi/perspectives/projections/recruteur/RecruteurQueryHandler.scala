package fr.poleemploi.perspectives.projections.recruteur

import fr.poleemploi.cqrs.projection.QueryHandler

import scala.concurrent.Future

class RecruteurQueryHandler(recruteurProjection: RecruteurProjection) extends QueryHandler {

  def getRecruteur(query: GetRecruteurQuery): Future[RecruteurDto] =
    recruteurProjection.getRecruteur(query)

  def listerParDateInscription(): Future[List[RecruteurDto]] =
    recruteurProjection.listerParDateInscription
}
