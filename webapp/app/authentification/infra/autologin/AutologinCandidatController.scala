package authentification.infra.autologin

import authentification._
import conf.WebAppConfig
import controllers.FlashMessages._
import fr.poleemploi.perspectives.authentification.infra.autologin.AutologinService
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{Email, Nom, Prenom}
import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AutologinCandidatController @Inject()(cc: ControllerComponents,
                                            webAppConfig: WebAppConfig,
                                            autologinService: AutologinService,
                                            messagesAction: MessagesActionBuilder,
                                            conseillerAdminAuthentifieAction: ConseillerAdminAuthentifieAction,
                                            candidatAutologgeAction: CandidatAutologgeAction)
                                           (implicit exec: ExecutionContext) extends AbstractController(cc) with Logging {

  def deconnexion: Action[AnyContent] = candidatAutologgeAction.async { candidatAutologgeRequest: CandidatAutologgeRequest[AnyContent] =>
    Future(Redirect(controllers.candidat.routes.LandingController.landing()).withSession(
      SessionCandidatAuthentifie.remove(SessionCandidatAutologge.remove(candidatAutologgeRequest.session))
    ).flashing(candidatAutologgeRequest.flash.withCandidatDeconnecte))
  }

  def genererTokenAutologin: Action[AnyContent] = conseillerAdminAuthentifieAction.async { conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      GenererCandidatAutologinTokenForm.form.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
        form => {
          val token = autologinService.genererTokenCandidat(CandidatId(form.candidatId), Nom(form.nom), Prenom(form.prenom), Email(form.email))
          Future(Ok(Json.obj("token" -> token.value)))
        }
      )
    }(conseillerRequest)
  }
}
