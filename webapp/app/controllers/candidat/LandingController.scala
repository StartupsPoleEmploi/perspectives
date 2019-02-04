package controllers.candidat

import authentification.infra.play.{OptionalCandidatAuthentifieAction, OptionalCandidatAuthentifieRequest, OptionalRecruteurAuthentifieAction, OptionalRecruteurAuthentifieRequest}
import conf.WebAppConfig
import controllers.AssetsFinder
import fr.poleemploi.perspectives.projections.rechercheCandidat.RechercheCandidatQueryHandler
import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._

@Singleton
class LandingController @Inject()(cc: ControllerComponents,
                                  implicit val assets: AssetsFinder,
                                  implicit val webAppConfig: WebAppConfig,
                                  optionalCandidatAuthentifieAction: OptionalCandidatAuthentifieAction,
                                  optionalRecruteurAuthentifieAction: OptionalRecruteurAuthentifieAction,
                                  rechercheCandidatQueryHandler: RechercheCandidatQueryHandler) extends AbstractController(cc) {

  def landing(): Action[AnyContent] = optionalRecruteurAuthentifieAction.async { optionalRecruteurAuthentifieRequest: OptionalRecruteurAuthentifieRequest[AnyContent] =>
    optionalCandidatAuthentifieAction { implicit request: OptionalCandidatAuthentifieRequest[AnyContent] =>
      if (request.isCandidatAuthentifie)
        Redirect(routes.OffreController.listeOffres())
      else
        Ok(views.html.candidat.landing(
          recruteurAuthentifie = optionalRecruteurAuthentifieRequest.recruteurAuthentifie,
          jsData = Json.obj(
            "secteursActivites" -> rechercheCandidatQueryHandler.secteursProposesDtos
          )
        ))
    }(optionalRecruteurAuthentifieRequest)
  }
}
