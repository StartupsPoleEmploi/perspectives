package controllers.candidat

import authentification._
import conf.WebAppConfig
import controllers.AssetsFinder
import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class LandingController @Inject()(cc: ControllerComponents,
                                  implicit val assets: AssetsFinder,
                                  implicit val webAppConfig: WebAppConfig,
                                  optionalCandidatAuthentifieAction: OptionalCandidatAuthentifieAction,
                                  optionalRecruteurAuthentifieAction: OptionalRecruteurAuthentifieAction)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def landing: Action[AnyContent] = optionalRecruteurAuthentifieAction.async { optionalRecruteurAuthentifieRequest: OptionalRecruteurAuthentifieRequest[AnyContent] =>
    optionalCandidatAuthentifieAction { implicit optionalCandidatAuthentifieRequest: OptionalCandidatAuthentifieRequest[AnyContent] =>
      if (optionalCandidatAuthentifieRequest.isCandidatAuthentifie)
        Redirect(controllers.candidat.routes.RechercheOffreController.index())
      else if (optionalRecruteurAuthentifieRequest.isRecruteurAuthentifie)
        Redirect(controllers.recruteur.routes.RechercheCandidatController.index())
      else
          Ok(views.html.candidat.landing(
            jsData = Json.obj(
              "algoliaPlacesConfig" -> webAppConfig.algoliaPlacesConfig
            )
          ))
    }(optionalRecruteurAuthentifieRequest)
  }
}
