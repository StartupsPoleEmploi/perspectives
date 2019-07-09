package fr.poleemploi.perspectives.projections.candidat.cv

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.CVId

case class DetailsCVCandidatQuery(candidatId: CandidatId) extends Query[DetailsCVCandidatQueryResult]

case class DetailsCVCandidatQueryResult(cvId: Option[CVId],
                                        nomFichier: Option[String]) extends QueryResult
