package fr.poleemploi.perspectives.projections.geo

import fr.poleemploi.cqrs.projection.{Query, QueryHandler, QueryResult}
import fr.poleemploi.perspectives.commun.geo.domain.ReferentielRegion

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegionQueryHandler(referentielRegion: ReferentielRegion) extends QueryHandler {

  override def configure: PartialFunction[Query[_ <: QueryResult], Future[QueryResult]] = {
    case RegionsQuery => referentielRegion.regions.map(RegionsQueryResult)
    case DepartementsQuery => referentielRegion.departements.map(DepartementsQueryResult)
  }
}