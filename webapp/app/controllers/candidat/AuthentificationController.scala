package controllers.candidat

import authentification._
import conf.WebAppConfig
import javax.inject.{Inject, Singleton}
import play.api.mvc._

@Singleton
class AuthentificationController @Inject()(cc: ControllerComponents,
                                           webappConfig: WebAppConfig,
                                           candidatAuthentifieAction: CandidatAuthentifieAction,
                                           peConnectController: PEConnectController) extends AbstractController(cc) {

  def deconnexion: Action[AnyContent] =
    if (webappConfig.usePEConnect)
      peConnectController.deconnexion
    else
      candidatAuthentifieAction { implicit request: CandidatAuthentifieRequest[AnyContent] =>
        Redirect(routes.LandingController.landing())
          .withSession(SessionCandidatAuthentifie.remove(request.session))
      }
}