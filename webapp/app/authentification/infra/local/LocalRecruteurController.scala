package authentification.infra.local

import authentification._
import controllers.FlashMessages._
import controllers.recruteur.routes
import fr.poleemploi.perspectives.authentification.domain.RecruteurAuthentifie
import fr.poleemploi.perspectives.commun.domain.{Email, Genre, Nom, Prenom}
import fr.poleemploi.perspectives.recruteur.{InscrireRecruteurCommand, RecruteurCommandHandler}
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext


@Singleton
class LocalRecruteurController @Inject()(cc: ControllerComponents,
                                         recruteurNonAuthentifieAction: RecruteurNonAuthentifieAction,
                                         recruteurAuthentifieAction: RecruteurAuthentifieAction,
                                         recruteurCommandHandler: RecruteurCommandHandler)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def connexion: Action[AnyContent] = recruteurNonAuthentifieAction.async { implicit request =>
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

  def deconnexion: Action[AnyContent] = recruteurAuthentifieAction { implicit request =>
    Redirect(routes.LandingController.landing())
      .withSession(SessionRecruteurAuthentifie.remove(request.session))
  }
}
