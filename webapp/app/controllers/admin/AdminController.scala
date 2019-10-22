package controllers.admin

import authentification._
import conf.WebAppConfig
import controllers.admin.AdminController.API_KEY
import fr.poleemploi.perspectives.rome.domain.ReferentielRome
import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AdminController @Inject()(cc: ControllerComponents,
                                webAppConfig: WebAppConfig,
                                referentielRome: ReferentielRome,
                                messagesAction: MessagesActionBuilder,
                                conseillerAdminAuthentifieAction: ConseillerAdminAuthentifieAction)
                               (implicit exec: ExecutionContext) extends AbstractController(cc) with Logging {

  def rechargerAppellationsMetiers: Action[AnyContent] = Action.async { implicit request =>
    if (isAutorise(request))
      referentielRome.rechargerAppellations.map(_ => NoContent)
    else Future(Unauthorized)
  }

  private def isAutorise(request: Request[AnyContent]): Boolean =
    request.getQueryString(API_KEY).contains(webAppConfig.adminApiKey)
}

object AdminController {
  private val API_KEY = "apiKey"
}
