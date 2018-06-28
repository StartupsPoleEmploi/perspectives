package fr.poleemploi.perspectives.projections

import fr.poleemploi.cqrs.projection.Query

case class FindCandidatQuery(peConnectId: String) extends Query

case class GetCandidatQuery(candidatId: String) extends Query
