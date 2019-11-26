package controllers.candidat

import authentification.{CandidatAuthentifieAction, CandidatAuthentifieRequest}
import conf.WebAppConfig
import controllers.AssetsFinder
import fr.poleemploi.perspectives.candidat.mrs.domain.ReferentielMRS
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc._
import tracking.TrackingService

import scala.concurrent.{ExecutionContext, Future}

class CandidatController @Inject()(components: ControllerComponents,
                                   implicit val assets: AssetsFinder,
                                   implicit val webAppConfig: WebAppConfig,
                                   messagesAction: MessagesActionBuilder,
                                   referentielMRS: ReferentielMRS,
                                   candidatAuthentifieAction: CandidatAuthentifieAction)(implicit exec: ExecutionContext) extends AbstractController(components) {

  def candidatSansMRS: Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      Future(Ok(views.html.candidat.candidatSansMrs(
        candidatAuthentifie = candidatAuthentifieRequest.candidatAuthentifie,
        gtmDataLayer = TrackingService.buildTrackingCandidat(
          optCandidatAuthentifie = None,
          flash = Some(messagesRequest.flash)
        )
      )))
    }(candidatAuthentifieRequest)
  }

  def listeMRS: Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      referentielMRS.mrsValidees(candidatAuthentifieRequest.candidatId).map(mrs =>
        if (mrs.isEmpty && webAppConfig.candidatsConseillers.contains(candidatAuthentifieRequest.candidatId)) // pour que les admins aient acces sans MRS
          Ok(Json.obj("nbMRSValidees" -> 1))
        else
          Ok(Json.obj("nbMRSValidees" -> mrs.size))
      )
    }(candidatAuthentifieRequest)
  }
}
