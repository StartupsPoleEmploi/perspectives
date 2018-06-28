package controllers.recruteur

import java.util.UUID

import authentification.infra.play.SessionRecruteurAuthentifie
import authentification.model.RecruteurAuthentifie
import conf.WebAppConfig
import fr.poleemploi.eventsourcing.AggregateId
import fr.poleemploi.perspectives.domain.Genre
import fr.poleemploi.perspectives.domain.recruteur.{InscrireRecruteurCommand, RecruteurCommandHandler}
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class InscriptionController@Inject()(cc: ControllerComponents,
                                     webappConfig: WebAppConfig,
                                     recruteurCommandHandler: RecruteurCommandHandler,
                                     peConnectController: PEConnectController) extends AbstractController(cc) {

  def inscription(): Action[AnyContent] =
    if (webappConfig.usePEConnect) {
      peConnectController.inscription()
    } else inscriptionSimple()

  private def inscriptionSimple(): Action[AnyContent] = Action.async { implicit request =>
    val aggregateId = AggregateId(UUID.randomUUID().toString)
    val command = InscrireRecruteurCommand(
      id = aggregateId,
      nom = "michu",
      prenom = "robert",
      email = "robert.michu@maboite.com",
      genre = Genre.HOMME
    )
    recruteurCommandHandler.inscrire(command).map { _ =>
      val recruteurAuthentifie = RecruteurAuthentifie(
        recruteurId = command.id.value,
        nom = command.nom,
        prenom = command.prenom
      )
      Redirect(routes.ProfilController.modificationProfil())
        .withSession(SessionRecruteurAuthentifie.set(recruteurAuthentifie, request.session))
    }
  }
}
