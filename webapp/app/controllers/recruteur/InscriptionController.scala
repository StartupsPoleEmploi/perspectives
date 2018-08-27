package controllers.recruteur

import authentification.infra.play.{RecruteurAuthentifieAction, RecruteurAuthentifieRequest, SessionRecruteurAuthentifie}
import conf.WebAppConfig
import controllers.FlashMessages._
import fr.poleemploi.perspectives.domain.Genre
import fr.poleemploi.perspectives.domain.authentification.RecruteurAuthentifie
import fr.poleemploi.perspectives.domain.recruteur.{InscrireRecruteurCommand, RecruteurCommandHandler}
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class InscriptionController @Inject()(cc: ControllerComponents,
                                      implicit val webappConfig: WebAppConfig,
                                      recruteurCommandHandler: RecruteurCommandHandler,
                                      recruteurAuthentifieAction: RecruteurAuthentifieAction,
                                      peConnectController: PEConnectController) extends AbstractController(cc) {

  def inscription(): Action[AnyContent] =
    if (webappConfig.usePEConnect) {
      peConnectController.inscription()
    } else inscriptionSimple()

  private def inscriptionSimple(): Action[AnyContent] = Action.async { implicit request =>
    val recruteurId = recruteurCommandHandler.newRecruteurId
    val command = InscrireRecruteurCommand(
      id = recruteurId,
      nom = "michu",
      prenom = "robert",
      email = "robert.michu@maboite.com",
      genre = Genre.HOMME
    )
    recruteurCommandHandler.inscrire(command).map { _ =>
      val recruteurAuthentifie = RecruteurAuthentifie(
        recruteurId = command.id,
        nom = command.nom,
        prenom = command.prenom
      )
      Redirect(routes.ProfilController.modificationProfil())
        .withSession(SessionRecruteurAuthentifie.set(recruteurAuthentifie, request.session))
        .flashing(request.flash.withRecruteurInscrit)
    }
  }

  def confirmationInscription(): Action[AnyContent] = recruteurAuthentifieAction.async { implicit recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    Future(Ok(views.html.recruteur.confirmationInscription(recruteurAuthentifieRequest.recruteurAuthentifie)))
  }
}
