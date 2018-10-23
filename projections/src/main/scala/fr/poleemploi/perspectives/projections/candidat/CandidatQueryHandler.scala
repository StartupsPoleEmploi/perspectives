package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{QueryHandler, UnauthorizedQueryException}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.{CV, CVService}
import fr.poleemploi.perspectives.candidat.mrs.domain.ReferentielMRSCandidat
import fr.poleemploi.perspectives.commun.domain.Metier
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.projections.recruteur.RecruteurProjection
import fr.poleemploi.perspectives.recruteur.TypeRecruteur

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatQueryHandler(candidatProjection: CandidatProjection,
                           recruteurProjection: RecruteurProjection,
                           cvService: CVService,
                           referentielMRSCandidat: ReferentielMRSCandidat,
                           referentielMetier: ReferentielMetier) extends QueryHandler {

  def candidatSaisieCriteresRecherche(query: CandidatSaisieCriteresRechercheQuery): Future[CandidatSaisieCriteresRechercheDto] =
    candidatProjection.candidatSaisieCriteresRecherche(query)

  def listerPourConseiller(query: CandidatsPourConseillerQuery): Future[CandidatsPourConseillerQueryResult] =
    candidatProjection.listerPourConseiller(query)

  def cvCandidat(query: CVCandidatQuery): Future[CV] =
    cvService.getCVByCandidat(query.candidatId)

  def cvCandidatPourRecruteur(query: CVCandidatPourRecruteurQuery): Future[CV] = {
    val autorisation = for {
      typeRecruteur <- recruteurProjection.typeRecruteur(query.recruteurId)
      candidatContactRecruteur <- candidatProjection.candidatContactRecruteur(query.candidatId)
      estAutorise = typeRecruteur match {
        case Some(TypeRecruteur.ENTREPRISE) => true
        case Some(TypeRecruteur.ORGANISME_FORMATION) => candidatContactRecruteur.contacteParOrganismeFormation.getOrElse(false)
        case Some(TypeRecruteur.AGENCE_INTERIM) => candidatContactRecruteur.contacteParAgenceInterim.getOrElse(false)
        case _ => false
      }
    } yield {
      if (!estAutorise) throw UnauthorizedQueryException(s"Le recruteur ${query.recruteurId.value} de type ${typeRecruteur.map(_.value)} n'est pas autorisé à récupérer le cv du candidat ${query.candidatId.value}")
    }

    autorisation.flatMap(_ => cvService.getCVByCandidat(query.candidatId))
  }

  def rechercherCandidatParDepartement(query: RechercherCandidatsParDepartementQuery): Future[ResultatRechercheCandidatParDepartement] =
    candidatProjection.rechercherCandidatParDepartement(query)

  def rechercherCandidatsParSecteur(query: RechercherCandidatsParSecteurQuery): Future[ResultatRechercheCandidat] =
    candidatProjection.rechercherCandidatParSecteur(query)

  def rechercherCandidatsParMetier(query: RechercherCandidatsParMetierQuery): Future[ResultatRechercheCandidatParMetier] =
    candidatProjection.rechercherCandidatParMetier(query)

  def metiersEvaluesNouvelInscrit(candidatId: CandidatId): Future[List[Metier]] =
    referentielMRSCandidat
      .mrsValideesParCandidat(candidatId)
      .map(mrsValidees => mrsValidees.map(m => referentielMetier.metierParCode(m.codeROME)))

}
