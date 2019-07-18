package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection._
import fr.poleemploi.perspectives.candidat.cv.domain.{CV, CVService}
import fr.poleemploi.perspectives.offre.domain.ReferentielOffre
import fr.poleemploi.perspectives.projections.candidat.cv._
import fr.poleemploi.perspectives.projections.recruteur.{RecruteurProjection, TypeRecruteurQuery}
import fr.poleemploi.perspectives.recruteur.TypeRecruteur

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatQueryHandler(candidatProjection: CandidatProjection,
                           recruteurProjection: RecruteurProjection,
                           cvService: CVService,
                           referentielOffre: ReferentielOffre) extends QueryHandler {

  override def configure: PartialFunction[Query[_ <: QueryResult], Future[QueryResult]] = {
    case q: TelechargerCVCandidatQuery => cvService.getCVByCandidat(q.candidatId).map(cv => TelechargerCVCandidatQueryResult(
      data = cv.data,
      typeMedia = cv.typeMedia
    ))
    case q: DetailsCVCandidatQuery => cvService.findDetailsCVByCandidat(q.candidatId).map(cv => DetailsCVCandidatQueryResult(
      cvId = cv.map(_.id),
      nomFichier = cv.map(_.nomFichier)
    ))
    case q: CVCandidatPourRecruteurQuery => cvCandidatPourRecruteur(q).map(CVCandidatPourRecruteurQueryResult)
    case q: CandidatSaisieCriteresRechercheQuery => candidatProjection.saisieCriteresRecherche(q)
    case q: CandidatLocalisationQuery => candidatProjection.localisation(q)
    case q: CandidatsPourConseillerQuery => candidatProjection.listerPourConseiller(q)
    case q: RechercheCandidatsQuery => candidatProjection.rechercherCandidats(q)
    case q: CandidatMetiersValidesQuery => candidatProjection.metiersValides(q)
    case q: OffresCandidatQuery => referentielOffre.rechercherOffres(q.criteresRechercheOffre)
      .map(r => OffresCandidatQueryResult(offres = r.offres, pageSuivante = r.pageSuivante))
    case q: CandidatPourRechercheOffreQuery => candidatProjection.rechercheOffre(q)
    case q: SecteursActivitesAvecCandidatsQuery => candidatProjection.secteursActivitesAvecCandidats(q)
  }

  private def cvCandidatPourRecruteur(query: CVCandidatPourRecruteurQuery): Future[CV] = {
    val autorisation = for {
      typeRecruteurResult <- recruteurProjection.typeRecruteur(TypeRecruteurQuery(query.recruteurId))
      candidatContactRecruteur <- candidatProjection.candidatContactRecruteur(query.candidatId)
      estAutorise = typeRecruteurResult.typeRecruteur match {
        case Some(TypeRecruteur.ENTREPRISE) => candidatContactRecruteur.contactRecruteur.getOrElse(false)
        case Some(TypeRecruteur.ORGANISME_FORMATION) => candidatContactRecruteur.contactFormation.getOrElse(false)
        case _ => false
      }
    } yield {
      if (!estAutorise) throw UnauthorizedQueryException(s"Le recruteur ${query.recruteurId.value} de type ${typeRecruteurResult.typeRecruteur.map(_.value)} n'est pas autorisé à récupérer le cv du candidat ${query.candidatId.value}")
    }

    autorisation.flatMap(_ => cvService.getCVByCandidat(query.candidatId))
  }
}
