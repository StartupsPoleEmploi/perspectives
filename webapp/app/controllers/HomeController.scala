package controllers

import java.util.UUID

import fr.poleemploi.eventsourcing.AggregateId
import fr.poleemploi.perspectives.domain.demandeurEmploi.{DemandeurEmploiCommandHandler, InscrireDemandeurEmploiCommand}
import javax.inject._
import play.api.mvc._

@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               commandHandler: DemandeurEmploiCommandHandler) extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def inscription() = Action { implicit request: Request[AnyContent] =>
    // TODO : gestion des demandeurs déjà inscrits?
    val aggegateId = AggregateId(UUID.randomUUID().toString)
    val command = InscrireDemandeurEmploiCommand(
      id = aggegateId,
      nom = "Plantu",
      prenom = "Robert",
      email = "robert.plantu@mail.com"
    )
    commandHandler.execute(command, a => a.inscrire())

    NoContent
  }
}
