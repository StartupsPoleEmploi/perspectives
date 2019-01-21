package controllers.candidat

import authentification.infra.play.{CandidatAuthentifieAction, CandidatAuthentifieRequest}
import conf.WebAppConfig
import controllers.AssetsFinder
import controllers.FlashMessages._
import fr.poleemploi.perspectives.offre.domain.{CriteresRechercheOffre, Experience}
import fr.poleemploi.perspectives.projections.candidat.OffresCandidatQueryResult._
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.projections.rechercheCandidat.RechercheCandidatQueryHandler
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class OffreController @Inject()(cc: ControllerComponents,
                                implicit val assets: AssetsFinder,
                                implicit val webAppConfig: WebAppConfig,
                                candidatAuthentifieAction: CandidatAuthentifieAction,
                                candidatQueryHandler: CandidatQueryHandler,
                                rechercheCandidatQueryHandler: RechercheCandidatQueryHandler) extends AbstractController(cc) {

  def listeOffres: Action[AnyContent] = candidatAuthentifieAction.async { implicit candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    for {
      criteresRechercheQueryResult <- candidatQueryHandler.handle(CandidatCriteresRechercheQuery(candidatAuthentifieRequest.candidatId))
      criteresRecherches = candidatAuthentifieRequest.request.flash.criteresRecherchesModifies.map(criteresModifies =>
        criteresRechercheQueryResult.copy(
          rechercheAutresMetiers = Some(criteresModifies.rechercheAutresMetiers),
          rechercheMetiersEvalues = Some(criteresModifies.rechercheMetiersEvalues),
          metiersRecherches = criteresModifies.metiersRecherches.flatMap(rechercheCandidatQueryHandler.metierProposeParCode).toList,
          rayonRecherche = Some(criteresModifies.rayonRecherche)
        )
      ).getOrElse(criteresRechercheQueryResult)
      offresCandidatQueryResult <-
        if (criteresRecherches.criteresComplet)
          candidatQueryHandler.handle(OffresCandidatQuery(buildCriteresRechercheOffre(criteresRecherches)))
        else
          Future.successful(OffresCandidatQueryResult(Nil))
    } yield {
      Ok(views.html.candidat.listeOffres(
        candidatAuthentifie = candidatAuthentifieRequest.candidatAuthentifie,
        jsData = Json.obj(
          "candidat" -> criteresRechercheQueryResult,
          "offres" -> offresCandidatQueryResult.offres,
          "secteursActivites" -> rechercheCandidatQueryHandler.secteursProposesDtos
        )
      ))
    }
  }

  private def buildCriteresRechercheOffre(candidatCriteresRechercheResult: CandidatCriteresRechercheQueryResult): CriteresRechercheOffre =
    CriteresRechercheOffre(
      codesROME = ((if (candidatCriteresRechercheResult.rechercheMetiersEvalues.contains(true))
        candidatCriteresRechercheResult.metiersEvalues
      else Nil) ++ (if (candidatCriteresRechercheResult.rechercheAutresMetiers.contains(true))
        candidatCriteresRechercheResult.metiersRecherches
      else Nil)).map(_.codeROME),
      codePostal = candidatCriteresRechercheResult.codePostal.get,
      rayonRecherche = candidatCriteresRechercheResult.rayonRecherche.get,
      experience = Experience.DEBUTANT
    )
}
