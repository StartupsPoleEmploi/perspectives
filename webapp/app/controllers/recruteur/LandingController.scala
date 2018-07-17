package controllers.recruteur

import authentification.infra.play.{OptionalRecruteurAuthentifieAction, OptionalRecruteurAuthentifieRequest}
import conf.WebAppConfig
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents}

@Singleton
class LandingController @Inject()(cc: ControllerComponents,
                                  implicit val webAppConfig: WebAppConfig,
                                  optionalRecruteurAuthentifieAction: OptionalRecruteurAuthentifieAction) extends AbstractController(cc) {

  def landing() = optionalRecruteurAuthentifieAction { implicit request: OptionalRecruteurAuthentifieRequest[AnyContent] =>
    Ok(views.html.recruteur.landing(request.recruteurAuthentifie))
  }
}