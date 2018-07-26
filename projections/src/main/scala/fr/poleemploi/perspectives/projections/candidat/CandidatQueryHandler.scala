package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.QueryHandler
import fr.poleemploi.perspectives.domain.candidat.CandidatId
import fr.poleemploi.perspectives.domain.candidat.cv.{CV, CVService, DetailsCV}

import scala.concurrent.Future

class CandidatQueryHandler(candidatProjection: CandidatProjection,
                           cvService: CVService) extends QueryHandler {

  def getCandidat(query: GetCandidatQuery): Future[CandidatDto] =
    candidatProjection.getCandidat(query.candidatId)

  def findAllOrderByDateInscription(): Future[List[CandidatDto]] =
    candidatProjection.findAllOrderByDateInscription

  def findDetailsCvByCandidat(query: FindDetailsCVByCandidatQuery): Future[Option[DetailsCV]] =
    cvService.findDetailsCvByCandidat(CandidatId(query.candidatId))

  def getCVByCandidat(query: GetCVByCandidatQuery): Future[CV] =
    cvService.getCvByCandidat(CandidatId(query.candidatId))

}
