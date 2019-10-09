package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.offre.domain._
import play.api.libs.json.{Json, Writes}

case class OffreCandidatQuery(offreId: OffreId) extends Query[OffreCandidatQueryResult]

case class OffreCandidatQueryResult(offre: Option[Offre]) extends QueryResult

object OffreCandidatQueryResult {

  implicit val writes: Writes[OffreCandidatQueryResult] = Json.writes[OffreCandidatQueryResult]
}
