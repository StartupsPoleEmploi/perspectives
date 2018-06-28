package controllers.conseiller

import authentification.infra.play.{ConseillerAuthentifieAction, ConseillerAuthentifieRequest}
import conf.WebAppConfig
import fr.poleemploi.perspectives.domain.conseiller.{AutorisationService, RoleConseiller}
import fr.poleemploi.perspectives.projections.candidat.CandidatQueryHandler
import fr.poleemploi.perspectives.projections.recruteur.RecruteurQueryHandler
import javax.inject.Inject
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConseillerController @Inject()(cc: ControllerComponents,
                                     implicit val webAppConfig: WebAppConfig,
                                     conseillerAuthentifieAction: ConseillerAuthentifieAction,
                                     autorisationService: AutorisationService,
                                     candidatQueryHandler: CandidatQueryHandler,
                                     recruteurQueryHandler: RecruteurQueryHandler) extends AbstractController(cc) {

  def listeCandidats(): Action[AnyContent] = conseillerAuthentifieAction.async { implicit conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    if (autorisationService.hasRole(conseillerRequest.conseillerId, RoleConseiller.ADMIN)) {
      candidatQueryHandler.findAllOrderByDateInscription()
        .map(candidats =>
          Ok(views.html.conseiller.listeCandidats(candidats))
        )
    } else Future.successful(Unauthorized)
  }

  def listeRecruteurs(): Action[AnyContent] = conseillerAuthentifieAction.async { implicit conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    if (autorisationService.hasRole(conseillerRequest.conseillerId, RoleConseiller.ADMIN)) {
      recruteurQueryHandler.findAllOrderByDateInscription()
        .map(recruteurs =>
          Ok(views.html.conseiller.listeRecruteurs(recruteurs))
        )
    } else Future.successful(Unauthorized)
  }

}
