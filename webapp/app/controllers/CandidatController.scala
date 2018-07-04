package controllers

import authentification.{AuthenticatedAction, CandidatRequest, Role, RoleService}
import conf.WebAppConfig
import fr.poleemploi.perspectives.projections.CandidatQueryHandler
import javax.inject.Inject
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatController @Inject()(cc: ControllerComponents,
                                   implicit val webAppConfig: WebAppConfig,
                                   authenticatedAction: AuthenticatedAction,
                                   roleService: RoleService,
                                   queryHandler: CandidatQueryHandler) extends AbstractController(cc) {

  def liste(): Action[AnyContent] = authenticatedAction.async { implicit candidatRequest: CandidatRequest[AnyContent] =>
    if (roleService.hasRole(candidatRequest.authenticatedCandidat, Role.ADMIN)) {
      queryHandler.findAllOrderByDateInscription()
        .map(candidats =>
          Ok(views.html.listeCandidats(candidatRequest.authenticatedCandidat, candidats))
        )
    } else Future.successful(Unauthorized)
  }
}
