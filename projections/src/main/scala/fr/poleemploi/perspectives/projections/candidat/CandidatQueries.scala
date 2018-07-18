package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.Query
import fr.poleemploi.perspectives.domain.candidat.cv.CVId

sealed trait CandidatQuery extends Query

case class GetCandidatQuery(candidatId: String) extends CandidatQuery

case class GetDetailsCVByCandidat(candidatId: String) extends CandidatQuery

case class GetFichierCVById(id: CVId) extends CandidatQuery