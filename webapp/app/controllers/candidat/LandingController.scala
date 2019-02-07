package controllers.candidat

import authentification.infra.play.{OptionalCandidatAuthentifieAction, OptionalCandidatAuthentifieRequest, OptionalRecruteurAuthentifieAction, OptionalRecruteurAuthentifieRequest}
import conf.WebAppConfig
import controllers.AssetsFinder
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import fr.poleemploi.perspectives.projections.candidat.CandidatQueryHandler
import fr.poleemploi.perspectives.projections.rechercheCandidat.RechercheCandidatQueryHandler
import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class LandingController @Inject()(cc: ControllerComponents,
                                  implicit val assets: AssetsFinder,
                                  implicit val webAppConfig: WebAppConfig,
                                  optionalCandidatAuthentifieAction: OptionalCandidatAuthentifieAction,
                                  optionalRecruteurAuthentifieAction: OptionalRecruteurAuthentifieAction,
                                  candidatQueryHandler: CandidatQueryHandler,
                                  rechercheCandidatQueryHandler: RechercheCandidatQueryHandler) extends AbstractController(cc) {

  def landing(): Action[AnyContent] = optionalRecruteurAuthentifieAction.async { optionalRecruteurAuthentifieRequest: OptionalRecruteurAuthentifieRequest[AnyContent] =>
    optionalCandidatAuthentifieAction.async { implicit optionalCandidatAuthentifieRequest: OptionalCandidatAuthentifieRequest[AnyContent] =>
        if (optionalCandidatAuthentifieRequest.isCandidatAuthentifie)
          Future.successful(Redirect(controllers.candidat.routes.OffreController.index()))
        else if (optionalRecruteurAuthentifieRequest.isRecruteurAuthentifie)
          Future.successful(Redirect(controllers.recruteur.routes.RechercheCandidatController.index()))
        else
          Future {
            val secteursAvecMetiers = rechercheCandidatQueryHandler.secteursProposes
            Ok(views.html.candidat.landing(
              jsData = Json.obj(
                "secteursActivites" -> secteursAvecMetiers,
                "metiers" -> secteursAvecMetiers.flatMap(_.metiers)
              )
            ))
          }
    }(optionalRecruteurAuthentifieRequest)
  }
}
