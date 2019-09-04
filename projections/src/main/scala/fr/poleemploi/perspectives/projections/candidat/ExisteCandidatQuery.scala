package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat._
import play.api.libs.json.{Json, Writes}

case class ExisteCandidatQuery(candidatId: CandidatId) extends Query[ExisteCandidatQueryResult]

case class ExisteCandidatQueryResult(existe: Boolean) extends QueryResult

object ExisteCandidatQueryResult {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[ExisteCandidatQueryResult] = Json.writes[ExisteCandidatQueryResult]
}
