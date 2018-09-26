package controllers.candidat

import authentification.infra.play.{OptionalCandidatAuthentifieAction, OptionalCandidatAuthentifieRequest}
import conf.WebAppConfig
import controllers.AssetsFinder
import fr.poleemploi.perspectives.projections.rechercheCandidat.RechercheCandidatQueryHandler
import javax.inject._
import play.api.mvc._

@Singleton
class LandingController @Inject()(cc: ControllerComponents,
                                  implicit val assets: AssetsFinder,
                                  implicit val webAppConfig: WebAppConfig,
                                  optionalCandidatAuthentifieAction: OptionalCandidatAuthentifieAction,
                                  rechercheCandidatQueryHandler: RechercheCandidatQueryHandler) extends AbstractController(cc) {

  def landing() = optionalCandidatAuthentifieAction { implicit request: OptionalCandidatAuthentifieRequest[AnyContent] =>
    Ok(views.html.candidat.landing(
      candidatAuthentifie = request.candidatAuthentifie,
      secteursActivites = rechercheCandidatQueryHandler.secteursProposes
    ))
  }
}
