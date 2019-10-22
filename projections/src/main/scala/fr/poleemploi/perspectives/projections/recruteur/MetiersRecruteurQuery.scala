package fr.poleemploi.perspectives.projections.recruteur

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.rome.domain.Appellation
import play.api.libs.json.{Json, Writes}

case class MetiersRecruteurQuery(query: String) extends Query[MetiersRecruteurQueryResult]

case class MetiersRecruteurQueryResult(metiers: Seq[Appellation]) extends QueryResult

object MetiersRecruteurQueryResult {

  implicit val writes: Writes[MetiersRecruteurQueryResult] = Json.writes[MetiersRecruteurQueryResult]
}
