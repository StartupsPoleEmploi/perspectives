package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.Query
import fr.poleemploi.perspectives.domain.candidat.CandidatId

sealed trait CandidatQuery extends Query

case class GetCandidatQuery(candidatId: CandidatId) extends CandidatQuery

case class FindDetailsCVByCandidatQuery(candidatId: CandidatId) extends CandidatQuery

case class GetCVByCandidatQuery(candidatId: CandidatId) extends CandidatQuery