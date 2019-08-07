package controllers.recruteur

import authentification._
import conf.WebAppConfig
import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.ExecutionContext

class AuthentificationController @Inject()(cc: ControllerComponents,
                                           webappConfig: WebAppConfig,
                                           peConnectController: PEConnectController,
                                           recruteurAuthentifieAction: RecruteurAuthentifieAction)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def deconnexion: Action[AnyContent] =
    if (webappConfig.usePEConnect)
      peConnectController.deconnexion
    else
      recruteurAuthentifieAction { implicit request: RecruteurAuthentifieRequest[AnyContent] =>
        Redirect(routes.LandingController.landing())
          .withSession(SessionRecruteurAuthentifie.remove(request.session))
      }
}
