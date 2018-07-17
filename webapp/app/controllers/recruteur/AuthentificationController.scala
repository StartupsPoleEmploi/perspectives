package controllers.recruteur

import authentification.infra.play.{RecruteurAuthentifieAction, RecruteurAuthentifieRequest, SessionRecruteurAuthentifie}
import conf.WebAppConfig
import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.Future

class AuthentificationController @Inject()(cc: ControllerComponents,
                                           webappConfig: WebAppConfig,
                                           peConnectController: PEConnectController,
                                           recruteurAuthentifieAction: RecruteurAuthentifieAction) extends AbstractController(cc) {

  def deconnexion(): Action[AnyContent] =
    if (webappConfig.usePEConnect) {
      peConnectController.deconnexion()
    } else recruteurAuthentifieAction.async { implicit request: RecruteurAuthentifieRequest[AnyContent] =>
      Future.successful(Redirect(routes.LandingController.landing()).withSession(
        SessionRecruteurAuthentifie.remove(request.session)
      ))
    }

}
