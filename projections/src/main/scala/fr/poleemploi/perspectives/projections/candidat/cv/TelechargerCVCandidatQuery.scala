package fr.poleemploi.perspectives.projections.candidat.cv

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.TypeMedia

case class TelechargerCVCandidatQuery(candidatId: CandidatId) extends Query[TelechargerCVCandidatQueryResult]

case class TelechargerCVCandidatQueryResult(data: Array[Byte],
                                            typeMedia: TypeMedia) extends QueryResult
