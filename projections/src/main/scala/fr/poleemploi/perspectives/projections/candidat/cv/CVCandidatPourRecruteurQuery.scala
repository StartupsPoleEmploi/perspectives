package fr.poleemploi.perspectives.projections.candidat.cv

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.CV
import fr.poleemploi.perspectives.recruteur.RecruteurId

case class CVCandidatPourRecruteurQuery(candidatId: CandidatId,
                                        recruteurId: RecruteurId) extends Query[CVCandidatPourRecruteurQueryResult]

case class CVCandidatPourRecruteurQueryResult(cv: CV) extends QueryResult
