package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain._
import play.api.libs.json._

case class CandidatsPourBatchJVRQuery(candidatIds: Seq[CandidatId]) extends Query[CandidatsPourBatchJVRQueryResult]

case class CandidatsPourBatchJVRQueryResult(candidats: List[CandidatPourBatchJVRDto]) extends QueryResult

object CandidatsPourBatchJVRQueryResult {

  implicit val writes: Writes[CandidatsPourBatchJVRQueryResult] = Json.writes[CandidatsPourBatchJVRQueryResult]
}

case class CandidatPourBatchJVRDto(candidatId: CandidatId,
                                   email: Email,
                                   nom: Nom,
                                   prenom: Prenom)

object CandidatPourBatchJVRDto {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[CandidatPourBatchJVRDto] = Json.writes[CandidatPourBatchJVRDto]
}
