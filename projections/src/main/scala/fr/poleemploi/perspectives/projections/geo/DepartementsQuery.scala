package fr.poleemploi.perspectives.projections.geo

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.commun.domain.Departement

case object DepartementsQuery extends Query[DepartementsQueryResult]

case class DepartementsQueryResult(result: List[Departement]) extends QueryResult