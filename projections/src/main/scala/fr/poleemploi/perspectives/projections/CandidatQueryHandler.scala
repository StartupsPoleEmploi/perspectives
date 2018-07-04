package fr.poleemploi.perspectives.projections

import fr.poleemploi.cqrs.projection.QueryHandler

import scala.concurrent.Future

class CandidatQueryHandler(candidatProjection: CandidatProjection) extends QueryHandler {

  def findCandidat(query: FindCandidatQuery): Future[Option[CandidatDto]] =
    candidatProjection.findCandidat(query.peConnectId)

  def getCandidat(query: GetCandidatQuery): Future[CandidatDto] =
    candidatProjection.getCandidat(query.candidatId)

  def findAllOrderByDateInscription(): Future[List[CandidatDto]] =
    candidatProjection.findAllOrderByDateInscription

}
