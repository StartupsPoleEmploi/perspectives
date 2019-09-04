package controllers.candidat

import authentification.infra.autologin.{AutologinCandidatController, OptionalCandidatAutologgeAction, OptionalCandidatAutologgeRequest}
import authentification.infra.local.LocalCandidatController
import authentification.infra.peconnect.PEConnectCandidatController
import conf.WebAppConfig
import fr.poleemploi.perspectives.commun.infra.Environnement
import javax.inject.{Inject, Singleton}
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class AuthentificationController @Inject()(cc: ControllerComponents,
                                           webAppConfig: WebAppConfig,
                                           optionalCandidatAutologgeAction: OptionalCandidatAutologgeAction,
                                           autologinCandidatController: AutologinCandidatController,
                                           peConnectController: PEConnectCandidatController,
                                           localController: LocalCandidatController)
                                          (implicit exec: ExecutionContext) extends AbstractController(cc) {

  def connexion: Action[AnyContent] =
    if (usePEConnect)
      peConnectController.connexion
    else
      localController.connexion

  def deconnexion: Action[AnyContent] = optionalCandidatAutologgeAction.async { optionalCandidatAutologgeRequest: OptionalCandidatAutologgeRequest[AnyContent] =>
    if (optionalCandidatAutologgeRequest.isCandidatAutologge)
      autologinCandidatController.deconnexion(optionalCandidatAutologgeRequest)
    else if (usePEConnect)
      peConnectController.deconnexion(optionalCandidatAutologgeRequest)
    else
      localController.deconnexion(optionalCandidatAutologgeRequest)
  }

  private def usePEConnect: Boolean =
    Environnement.PRODUCTION == webAppConfig.environnement || webAppConfig.usePEConnect

}
