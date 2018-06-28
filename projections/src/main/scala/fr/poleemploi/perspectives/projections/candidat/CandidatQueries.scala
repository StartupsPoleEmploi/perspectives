package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.Query

sealed trait CandidatQuery extends Query

case class GetCandidatQuery(candidatId: String) extends CandidatQuery