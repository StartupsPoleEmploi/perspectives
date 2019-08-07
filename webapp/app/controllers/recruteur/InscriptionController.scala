package controllers.recruteur

import authentification._
import conf.WebAppConfig
import controllers.FlashMessages._
import fr.poleemploi.perspectives.authentification.domain.RecruteurAuthentifie
import fr.poleemploi.perspectives.commun.domain.{Email, Genre, Nom, Prenom}
import fr.poleemploi.perspectives.recruteur.{InscrireRecruteurCommand, RecruteurCommandHandler}
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class InscriptionController @Inject()(cc: ControllerComponents,
                                      implicit val webappConfig: WebAppConfig,
                                      recruteurCommandHandler: RecruteurCommandHandler,
                                      recruteurAuthentifieAction: RecruteurAuthentifieAction,
                                      peConnectController: PEConnectController)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def inscription: Action[AnyContent] =
    if (webappConfig.usePEConnect)
      peConnectController.inscription
    else
      inscriptionSimple

  private def inscriptionSimple: Action[AnyContent] = Action.async { implicit request =>
    val recruteurId = recruteurCommandHandler.newId
    val command = InscrireRecruteurCommand(
      id = recruteurId,
      nom = Nom("michu"),
      prenom = Prenom("robert"),
      email = Email("robert.michu@maboite.com"),
      genre = Genre.HOMME
    )
    recruteurCommandHandler.handle(command).map { _ =>
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
}
