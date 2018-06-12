package controllers

import authentification.{OptionalAuthenticatedAction, OptionalCandidatRequest}
import conf.WebAppConfig
import javax.inject._
import play.api.mvc._

@Singleton
class LandingController @Inject()(cc: ControllerComponents,
                                  implicit val webAppConfig: WebAppConfig,
                                  optionalAuthenticatedAction: OptionalAuthenticatedAction) extends AbstractController(cc) {

  def landing() = optionalAuthenticatedAction { implicit request: OptionalCandidatRequest[AnyContent] =>
    Ok(views.html.landing(request.authenticatedCandidat))
  }
}
