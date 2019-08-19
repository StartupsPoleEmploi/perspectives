package fr.poleemploi.perspectives.projections.geo

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.commun.domain.Region

case object RegionsQuery extends Query[RegionsQueryResult]

case class RegionsQueryResult(result: List[Region]) extends QueryResult