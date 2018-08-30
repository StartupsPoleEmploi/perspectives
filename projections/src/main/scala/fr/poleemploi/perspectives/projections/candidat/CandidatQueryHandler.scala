package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{QueryHandler, UnauthorizedQueryException}
import fr.poleemploi.perspectives.candidat.cv.domain.{CV, CVService}
import fr.poleemploi.perspectives.projections.recruteur.{GetRecruteurQuery, RecruteurProjection}
import fr.poleemploi.perspectives.recruteur.TypeRecruteur

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatQueryHandler(candidatProjection: CandidatProjection,
                           recruteurProjection: RecruteurProjection,
                           cvService: CVService) extends QueryHandler {

  def getCandidat(query: GetCandidatQuery): Future[CandidatDto] =
    candidatProjection.getCandidat(query)

  def listerParDateInscription(): Future[List[CandidatDto]] =
    candidatProjection.listerParDateInscription

  def getCVParCandidat(query: GetCVParCandidatQuery): Future[CV] =
    cvService.getCVByCandidat(query.candidatId)

  def getCVPourRecruteurParCandidat(query: GetCVPourRecruteurParCandidatQuery): Future[CV] = {
    val autorisation = for {
      recruteur <- recruteurProjection.getRecruteur(GetRecruteurQuery(query.recruteurId))
      candidat <- candidatProjection.getCandidat(GetCandidatQuery(query.candidatId))
      estAutorise = recruteur.typeRecruteur match {
        case Some(TypeRecruteur.ORGANISME_FORMATION) => candidat.contacteParOrganismeFormation.getOrElse(true)
        case Some(TypeRecruteur.AGENCE_INTERIM) => candidat.contacteParAgenceInterim.getOrElse(true)
        case _ => true
      }
    } yield {
      if (!estAutorise) throw UnauthorizedQueryException(s"Le recruteur ${query.recruteurId.value} de type ${recruteur.typeRecruteur} n'est pas autorisé à récupérer le cv du candidat ${query.candidatId.value}")
    }

    autorisation.flatMap(_ => cvService.getCVByCandidat(query.candidatId))
  }

  def rechercheCandidatsParDateInscription(query: RechercherCandidatsParDateInscriptionQuery): Future[ResultatRechercheCandidatParDateInscription] =
    candidatProjection.rechercherCandidatParDateInscription(query)

  def rechercherCandidatsParSecteur(query: RechercherCandidatsParSecteurQuery): Future[ResultatRechercheCandidat] =
    candidatProjection.rechercherCandidatParSecteur(query)

  def rechercherCandidatsParMetier(query: RechercherCandidatsParMetierQuery): Future[ResultatRechercheCandidatParMetier] =
    candidatProjection.rechercherCandidatParMetier(query)
}
