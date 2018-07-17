package controllers.candidat

import authentification.infra.play.{OptionalCandidatAuthentifieAction, OptionalCandidatAuthentifieRequest}
import conf.WebAppConfig
import javax.inject._
import play.api.mvc._

@Singleton
class LandingController @Inject()(cc: ControllerComponents,
                                  implicit val webAppConfig: WebAppConfig,
                                  optionalCandidatAuthentifieAction: OptionalCandidatAuthentifieAction) extends AbstractController(cc) {

  def landing() = optionalCandidatAuthentifieAction { implicit request: OptionalCandidatAuthentifieRequest[AnyContent] =>
    Ok(views.html.candidat.landing(request.candidatAuthentifie))
  }
}
