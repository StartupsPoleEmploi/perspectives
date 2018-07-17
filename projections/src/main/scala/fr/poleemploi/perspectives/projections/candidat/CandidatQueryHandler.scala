package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.QueryHandler

import scala.concurrent.Future

class CandidatQueryHandler(candidatProjection: CandidatProjection) extends QueryHandler {

  def getCandidat(query: GetCandidatQuery): Future[CandidatDto] =
    candidatProjection.getCandidat(query.candidatId)

  def findAllOrderByDateInscription(): Future[List[CandidatDto]] =
    candidatProjection.findAllOrderByDateInscription

}
