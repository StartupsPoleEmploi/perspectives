package controllers.recruteur

import authentification._
import conf.WebAppConfig
import controllers.AssetsFinder
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import tracking.TrackingService

import scala.concurrent.ExecutionContext

@Singleton
class LandingController @Inject()(cc: ControllerComponents,
                                  implicit val assets: AssetsFinder,
                                  implicit val webAppConfig: WebAppConfig,
                                  optionalCandidatAuthentifieAction: OptionalCandidatAuthentifieAction,
                                  optionalRecruteurAuthentifieAction: OptionalRecruteurAuthentifieAction)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def landing: Action[AnyContent] = optionalCandidatAuthentifieAction.async { optionalCandidatAuthentifieRequest: OptionalCandidatAuthentifieRequest[AnyContent] =>
    optionalRecruteurAuthentifieAction { implicit optionalRecruteurAuthentifieRequest: OptionalRecruteurAuthentifieRequest[AnyContent] =>
      if (optionalRecruteurAuthentifieRequest.isRecruteurAuthentifie)
        Redirect(controllers.recruteur.routes.RechercheCandidatController.index())
      else if (optionalCandidatAuthentifieRequest.isCandidatAuthentifie)
        Redirect(controllers.candidat.routes.RechercheOffreController.index())
      else
        Ok(views.html.recruteur.landing(
          gtmDataLayer = TrackingService.buildTrackingRecruteur(
            optRecruteurAuthentifie = None,
            flash = Some(optionalRecruteurAuthentifieRequest.flash)
          )
        ))
    }(optionalCandidatAuthentifieRequest)
  }
}
