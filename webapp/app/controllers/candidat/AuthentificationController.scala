package controllers.candidat

import authentification.infra.play.{CandidatAuthentifieAction, CandidatAuthentifieRequest, SessionCandidatAuthentifie}
import conf.WebAppConfig
import javax.inject.{Inject, Singleton}
import play.api.mvc._

import scala.concurrent.Future

@Singleton
class AuthentificationController @Inject()(cc: ControllerComponents,
                                           webappConfig: WebAppConfig,
                                           candidatAuthentifieAction: CandidatAuthentifieAction,
                                           peConnectController: PEConnectController) extends AbstractController(cc) {

  def deconnexion(): Action[AnyContent] =
    if (webappConfig.usePEConnect) {
      peConnectController.deconnexion()
    } else candidatAuthentifieAction.async { implicit request: CandidatAuthentifieRequest[AnyContent] =>
      Future.successful(Redirect(routes.LandingController.landing()).withSession(
        SessionCandidatAuthentifie.remove(request.session)
      ))
    }
}
