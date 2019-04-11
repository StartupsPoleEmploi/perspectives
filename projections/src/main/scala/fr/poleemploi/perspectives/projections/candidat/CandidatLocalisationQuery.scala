package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.CandidatId
import play.api.libs.json.{Json, Writes}

case class CandidatLocalisationQuery(candidatId: CandidatId) extends Query[CandidatLocalisationQueryResult]

case class CandidatLocalisationQueryResult(commune: Option[String],
                                           codePostal: Option[String],
                                           latitude: Option[Double],
                                           longitude: Option[Double]) extends QueryResult

object CandidatLocalisationQueryResult {
  implicit val writes: Writes[CandidatLocalisationQueryResult] = Json.writes[CandidatLocalisationQueryResult]
}