package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain._
import play.api.libs.json._

case class CandidatsPourBatchDisponibilitesQuery(candidatIds: Stream[CandidatId]) extends Query[CandidatsPourBatchDisponibilitesQueryResult]

case class CandidatsPourBatchDisponibilitesQueryResult(candidats: List[CandidatPourBatchDisponibilitesDto]) extends QueryResult

object CandidatsPourBatchDisponibilitesQueryResult {

  implicit val writes: Writes[CandidatsPourBatchDisponibilitesQueryResult] = Json.writes[CandidatsPourBatchDisponibilitesQueryResult]
}

case class CandidatPourBatchDisponibilitesDto(candidatId: CandidatId,
                                              email: Email)

object CandidatPourBatchDisponibilitesDto {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[CandidatPourBatchDisponibilitesDto] = Json.writes[CandidatPourBatchDisponibilitesDto]
}
