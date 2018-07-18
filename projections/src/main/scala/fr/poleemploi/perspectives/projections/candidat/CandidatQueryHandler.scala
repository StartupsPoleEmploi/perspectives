package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.QueryHandler
import fr.poleemploi.perspectives.domain.candidat.CandidatId
import fr.poleemploi.perspectives.domain.candidat.cv.CVCandidat
import fr.poleemploi.perspectives.projections.candidat.cv.{CvProjection, DetailsCVDto, FichierCvDto}

import scala.concurrent.Future

class CandidatQueryHandler(candidatProjection: CandidatProjection,
                           cvProjection: CvProjection) extends QueryHandler {

  def getCandidat(query: GetCandidatQuery): Future[CandidatDto] =
    candidatProjection.getCandidat(query.candidatId)

  def findAllOrderByDateInscription(): Future[List[CandidatDto]] =
    candidatProjection.findAllOrderByDateInscription

  def findByCandidat(candidatId: String): Future[Option[CVCandidat]] =
    cvProjection.findByCandidat(CandidatId(candidatId))

  def getByCandidat(candidatId: String): Future[FichierCvDto] =
    cvProjection.getByCandidat(CandidatId(candidatId))

  def getDetailsCvCandidat(query: GetDetailsCVByCandidat): Future[Option[DetailsCVDto]] =
    cvProjection.findCvByCandidat(CandidatId(query.candidatId))

  def getFichierCv(query: GetFichierCVById): Future[FichierCvDto] =
    cvProjection.getFichierCv(query.id)

}
