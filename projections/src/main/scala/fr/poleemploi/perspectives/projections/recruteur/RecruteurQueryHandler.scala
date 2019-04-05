package fr.poleemploi.perspectives.projections.recruteur

import fr.poleemploi.cqrs.projection.{Query, QueryHandler, QueryResult}

import scala.concurrent.Future

class RecruteurQueryHandler(recruteurProjection: RecruteurProjection) extends QueryHandler {

  override def configure: PartialFunction[Query[_ <: QueryResult], Future[QueryResult]] = {
    case q: TypeRecruteurQuery => recruteurProjection.typeRecruteur(q)
    case q: ProfilRecruteurQuery => recruteurProjection.profilRecruteur(q)
    case q: RecruteursPourConseillerQuery => recruteurProjection.listerPourConseiller(q)
  }
}