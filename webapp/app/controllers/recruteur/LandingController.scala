package controllers.recruteur

import authentification.infra.play.{OptionalRecruteurAuthentifieAction, OptionalRecruteurAuthentifieRequest}
import conf.WebAppConfig
import controllers.AssetsFinder
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

@Singleton
class LandingController @Inject()(cc: ControllerComponents,
                                  implicit val assets: AssetsFinder,
                                  implicit val webAppConfig: WebAppConfig,
                                  optionalRecruteurAuthentifieAction: OptionalRecruteurAuthentifieAction) extends AbstractController(cc) {

  def landing(): Action[AnyContent] = optionalRecruteurAuthentifieAction { implicit request: OptionalRecruteurAuthentifieRequest[AnyContent] =>
    if (request.isRecruteurAuthentifie)
      Redirect(routes.RechercheCandidatController.index())
    else
      Ok(views.html.recruteur.landing())
  }
}