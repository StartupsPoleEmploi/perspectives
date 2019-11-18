package controllers

import authentification.{OptionalCandidatAuthentifieAction, OptionalCandidatAuthentifieRequest, OptionalRecruteurAuthentifieAction, OptionalRecruteurAuthentifieRequest}
import conf.WebAppConfig
import javax.inject._
import play.api.mvc._
import tracking.TrackingService

import scala.concurrent.ExecutionContext

@Singleton
class InfosLegalesController @Inject()(cc: ControllerComponents,
                                       implicit val assets: AssetsFinder,
                                       implicit val webAppConfig: WebAppConfig,
                                       optionalCandidatAuthentifieAction: OptionalCandidatAuthentifieAction,
                                       optionalRecruteurAuthentifieAction: OptionalRecruteurAuthentifieAction)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def index: Action[AnyContent] = optionalRecruteurAuthentifieAction.async { optionalRecruteurAuthentifieRequest: OptionalRecruteurAuthentifieRequest[AnyContent] =>
    optionalCandidatAuthentifieAction { implicit optionalCandidatAuthentifieRequest: OptionalCandidatAuthentifieRequest[AnyContent] =>
      Ok(views.html.infosLegales(
        candidatAuthentifie = optionalCandidatAuthentifieRequest.candidatAuthentifie,
        recruteurAuthentifie = optionalRecruteurAuthentifieRequest.recruteurAuthentifie,
        gtmDataLayer = TrackingService.buildTrackingCommun(
          optCandidatAuthentifie = optionalCandidatAuthentifieRequest.candidatAuthentifie,
          optRecruteurAuthentifie = optionalRecruteurAuthentifieRequest.recruteurAuthentifie
        )
      ))
    }(optionalRecruteurAuthentifieRequest)
  }
}
