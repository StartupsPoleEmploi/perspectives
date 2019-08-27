package fr.poleemploi.perspectives.projections.recruteur

import fr.poleemploi.cqrs.projection.{Query, QueryHandler, QueryResult}

import scala.concurrent.Future

class RecruteurQueryHandler(recruteurProjectionQuery: RecruteurProjectionQuery) extends QueryHandler {

  override def configure: PartialFunction[Query[_ <: QueryResult], Future[QueryResult]] = {
    case q: TypeRecruteurQuery => recruteurProjectionQuery.typeRecruteur(q)
    case q: ProfilRecruteurQuery => recruteurProjectionQuery.profilRecruteur(q)
    case q: RecruteursPourConseillerQuery => recruteurProjectionQuery.listerPourConseiller(q)
  }
}
