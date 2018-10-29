package fr.poleemploi.perspectives.projections.candidat.cv

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.CV

case class CVCandidatQuery(candidatId: CandidatId) extends Query[CVCandidatQueryResult]

case class CVCandidatQueryResult(cv: CV) extends QueryResult
