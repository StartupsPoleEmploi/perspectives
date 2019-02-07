package controllers.recruteur

import authentification.infra.play.{OptionalCandidatAuthentifieAction, OptionalCandidatAuthentifieRequest, OptionalRecruteurAuthentifieAction, OptionalRecruteurAuthentifieRequest}
import conf.WebAppConfig
import controllers.AssetsFinder
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class LandingController @Inject()(cc: ControllerComponents,
                                  implicit val assets: AssetsFinder,
                                  implicit val webAppConfig: WebAppConfig,
                                  optionalCandidatAuthentifieAction: OptionalCandidatAuthentifieAction,
                                  optionalRecruteurAuthentifieAction: OptionalRecruteurAuthentifieAction) extends AbstractController(cc) {

  def landing(): Action[AnyContent] = optionalRecruteurAuthentifieAction.async { optionalRecruteurAuthentifieRequest: OptionalRecruteurAuthentifieRequest[AnyContent] =>
    optionalCandidatAuthentifieAction.async { implicit optionalCandidatAuthentifieRequest: OptionalCandidatAuthentifieRequest[AnyContent] =>
      if (optionalCandidatAuthentifieRequest.isCandidatAuthentifie)
        Future.successful(Redirect(controllers.candidat.routes.OffreController.index()))
      else if (optionalRecruteurAuthentifieRequest.isRecruteurAuthentifie)
        Future.successful(Redirect(controllers.recruteur.routes.RechercheCandidatController.index()))
      else
        Future(Ok(views.html.recruteur.landing()))
    }(optionalRecruteurAuthentifieRequest)
  }
}