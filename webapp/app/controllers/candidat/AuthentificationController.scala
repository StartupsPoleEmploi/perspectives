package controllers.candidat

import authentification.infra.local.LocalCandidatController
import authentification.infra.peconnect.PEConnectCandidatController
import conf.WebAppConfig
import fr.poleemploi.perspectives.commun.infra.Environnement
import javax.inject.{Inject, Singleton}
import play.api.mvc._

@Singleton
class AuthentificationController @Inject()(cc: ControllerComponents,
                                           webAppConfig: WebAppConfig,
                                           peConnectController: PEConnectCandidatController,
                                           localController: LocalCandidatController) extends AbstractController(cc) {

  def connexion: Action[AnyContent] =
    if (usePEConnect)
      peConnectController.connexion
    else
      localController.connexion

  def deconnexion: Action[AnyContent] =
    if (usePEConnect)
      peConnectController.deconnexion
    else
      localController.deconnexion

  private def usePEConnect: Boolean =
    Environnement.PRODUCTION == webAppConfig.environnement || webAppConfig.usePEConnect
}