package controllers.recruteur

import authentification._
import conf.WebAppConfig
import controllers.AssetsFinder
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class LandingController @Inject()(cc: ControllerComponents,
                                  implicit val assets: AssetsFinder,
                                  implicit val webAppConfig: WebAppConfig,
                                  optionalCandidatAuthentifieAction: OptionalCandidatAuthentifieAction,
                                  optionalRecruteurAuthentifieAction: OptionalRecruteurAuthentifieAction)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def landing: Action[AnyContent] = optionalRecruteurAuthentifieAction.async { optionalRecruteurAuthentifieRequest: OptionalRecruteurAuthentifieRequest[AnyContent] =>
    optionalCandidatAuthentifieAction { implicit optionalCandidatAuthentifieRequest: OptionalCandidatAuthentifieRequest[AnyContent] =>
      if (optionalRecruteurAuthentifieRequest.isRecruteurAuthentifie)
        Redirect(controllers.recruteur.routes.RechercheCandidatController.index())
      else if (optionalCandidatAuthentifieRequest.isCandidatAuthentifie)
        Redirect(controllers.candidat.routes.RechercheOffreController.index())
      else
        Ok(views.html.recruteur.landing())
    }(optionalRecruteurAuthentifieRequest)
  }
}