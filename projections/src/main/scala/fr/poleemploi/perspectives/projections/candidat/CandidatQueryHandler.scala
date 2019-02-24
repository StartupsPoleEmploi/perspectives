package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryHandler, QueryResult, UnauthorizedQueryException}
import fr.poleemploi.perspectives.candidat.cv.domain.{CV, CVService}
import fr.poleemploi.perspectives.candidat.mrs.domain.{ReferentielHabiletesMRS, ReferentielMRSCandidat}
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.offre.domain.ReferentielOffre
import fr.poleemploi.perspectives.projections.candidat.cv.{CVCandidatPourRecruteurQuery, CVCandidatPourRecruteurQueryResult, CVCandidatQuery, CVCandidatQueryResult}
import fr.poleemploi.perspectives.projections.candidat.mrs.{CodeROMEParDepartementQuery, CodeROMEParDepartementQueryResult, MetiersEvaluesNouvelInscritQuery, MetiersEvaluesNouvelInscritQueryResult}
import fr.poleemploi.perspectives.projections.recruteur.{RecruteurProjection, TypeRecruteurQuery}
import fr.poleemploi.perspectives.recruteur.TypeRecruteur

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatQueryHandler(candidatProjection: CandidatProjection,
                           recruteurProjection: RecruteurProjection,
                           cvService: CVService,
                           referentielMRSCandidat: ReferentielMRSCandidat,
                           referentielMetier: ReferentielMetier,
                           referentielHabiletesMRS: ReferentielHabiletesMRS,
                           referentielOffre: ReferentielOffre) extends QueryHandler {

  override def configure: PartialFunction[Query[_ <: QueryResult], Future[QueryResult]] = {
    case q: CVCandidatQuery => cvService.getCVByCandidat(q.candidatId).map(CVCandidatQueryResult)
    case q: CVCandidatPourRecruteurQuery => cvCandidatPourRecruteur(q).map(CVCandidatPourRecruteurQueryResult)
    case q: CandidatSaisieCriteresRechercheQuery => candidatProjection.candidatSaisieCriteresRecherche(q)
    case q: CandidatCriteresRechercheQuery => candidatProjection.candidatCriteresRecherche(q)
    case q: CandidatsPourConseillerQuery => candidatProjection.listerPourConseiller(q)
    case q: RechercherCandidatsQuery => candidatProjection.rechercherCandidats(q)
    case q: MetiersEvaluesNouvelInscritQuery => metiersEvaluesNouvelInscrit(q)
    case _: CodeROMEParDepartementQuery => referentielHabiletesMRS.codeROMEsParDepartement.map(CodeROMEParDepartementQueryResult(_))
    case q: OffresCandidatQuery => referentielOffre.rechercherOffres(q.criteresRechercheOffre).map(OffresCandidatQueryResult)
    case q: CandidatPourRechercheOffreQuery => candidatProjection.candidatPourRechercheOffre(q)
  }

  private def cvCandidatPourRecruteur(query: CVCandidatPourRecruteurQuery): Future[CV] = {
    val autorisation = for {
      typeRecruteurResult <- recruteurProjection.typeRecruteur(TypeRecruteurQuery(query.recruteurId))
      candidatContactRecruteur <- candidatProjection.candidatContactRecruteur(query.candidatId)
      estAutorise = typeRecruteurResult.typeRecruteur match {
        case Some(TypeRecruteur.ENTREPRISE) => true
        case Some(TypeRecruteur.ORGANISME_FORMATION) => candidatContactRecruteur.contacteParOrganismeFormation.getOrElse(false)
        case Some(TypeRecruteur.AGENCE_INTERIM) => candidatContactRecruteur.contacteParAgenceInterim.getOrElse(false)
        case _ => false
      }
    } yield {
      if (!estAutorise) throw UnauthorizedQueryException(s"Le recruteur ${query.recruteurId.value} de type ${typeRecruteurResult.typeRecruteur.map(_.value)} n'est pas autorisé à récupérer le cv du candidat ${query.candidatId.value}")
    }

    autorisation.flatMap(_ => cvService.getCVByCandidat(query.candidatId))
  }

  private def metiersEvaluesNouvelInscrit(query: MetiersEvaluesNouvelInscritQuery): Future[MetiersEvaluesNouvelInscritQueryResult] =
    for {
      mrsValidees <- referentielMRSCandidat.mrsValideesParCandidat(query.candidatId)
      metiersEvalues <- referentielMetier.metiersParCode(mrsValidees.map(m => m.codeROME))
    } yield MetiersEvaluesNouvelInscritQueryResult(metiersEvalues)
}
