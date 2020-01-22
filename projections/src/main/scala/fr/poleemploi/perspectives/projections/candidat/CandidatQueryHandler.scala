package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection._
import fr.poleemploi.perspectives.offre.domain.ReferentielOffre
import fr.poleemploi.perspectives.projections.recruteur.RecruteurProjectionQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatQueryHandler(candidatProjectionQuery: CandidatProjectionQuery,
                           recruteurProjectionQuery: RecruteurProjectionQuery,
                           referentielOffre: ReferentielOffre) extends QueryHandler {

  override def configure: PartialFunction[Query[_ <: QueryResult], Future[QueryResult]] = {
    case q: CandidatSaisieCriteresRechercheQuery => candidatProjectionQuery.saisieCriteresRecherche(q)
    case q: CandidatSaisieDisponibilitesQuery => candidatProjectionQuery.saisieDisponibilites(q)
    case q: CandidatLocalisationQuery => candidatProjectionQuery.localisation(q)
    case q: CandidatsPourConseillerQuery => candidatProjectionQuery.listerPourConseiller(q)
    case q: RechercheCandidatsQuery => candidatProjectionQuery.rechercherCandidats(q)
    case q: CandidatMetiersValidesQuery => candidatProjectionQuery.metiersValides(q)
    case q: OffresCandidatQuery => referentielOffre.rechercherOffres(q.criteresRechercheOffre)
      .map(r => OffresCandidatQueryResult(offres = r.offres, pageSuivante = r.pageSuivante))
    case q: CandidatPourRechercheOffreQuery => candidatProjectionQuery.rechercheOffre(q)
    case q: SecteursActivitesAvecCandidatsQuery => candidatProjectionQuery.secteursActivitesAvecCandidats(q)
    case q: ExisteCandidatQuery => candidatProjectionQuery.existeCandidat(q)
    case q: CandidatsPourBatchDisponibilitesQuery => candidatProjectionQuery.listerPourBatchDisponibilites(q)
    case q: CandidatsPourBatchJVRQuery => candidatProjectionQuery.listerPourBatchJVR(q)
    case q: CandidatsPourCsvQuery.type => candidatProjectionQuery.listerPourCsv(q)
  }


}
