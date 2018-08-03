package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.QueryHandler
import fr.poleemploi.perspectives.domain.candidat.cv.{CV, CVService}

import scala.concurrent.Future

class CandidatQueryHandler(candidatProjection: CandidatProjection,
                           cvService: CVService) extends QueryHandler {

  def getCandidat(query: GetCandidatQuery): Future[CandidatDto] =
    candidatProjection.getCandidat(query)

  def findAllOrderByDateInscription(): Future[List[CandidatDto]] =
    candidatProjection.findAllOrderByDateInscription

  def getCVByCandidat(query: GetCVByCandidatQuery): Future[CV] =
    cvService.getCvByCandidat(query.candidatId)

  def rechercheCandidatsParDateInscription(query: RechercherCandidatsParDateInscriptionQuery): Future[ResultatRechercheCandidatParDateInscription] =
    candidatProjection.rechercherCandidatParDateInscription(query)

  def rechercherCandidatsParSecteur(query: RechercheCandidatsParSecteurQuery): Future[ResultatRechercheCandidat] =
    candidatProjection.rechercherCandidatParSecteur(query)

  def rechercherCandidatsParMetier(query: RechercherCandidatsParMetierQuery): Future[ResultatRechercheCandidatParMetier] =
    candidatProjection.rechercherCandidatParMetier(query)
}
