package authentification.infra.local

import authentification.{CandidatAuthentifieAction, CandidatNonAuthentifieAction, SessionCandidatAuthentifie}
import controllers.FlashMessages._
import controllers.candidat.routes
import fr.poleemploi.perspectives.authentification.domain.CandidatAuthentifie
import fr.poleemploi.perspectives.candidat.{CandidatCommandHandler, InscrireCandidatCommand}
import fr.poleemploi.perspectives.commun.domain.{Email, Genre, Nom, Prenom}
import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class LocalCandidatController @Inject()(cc: ControllerComponents,
                                        candidatNonAuthentifieAction: CandidatNonAuthentifieAction,
                                        candidatAuthentifieAction: CandidatAuthentifieAction,
                                        candidatCommandHandler: CandidatCommandHandler)(implicit exec: ExecutionContext) extends AbstractController(cc) with Logging {

  def connexion: Action[AnyContent] = candidatNonAuthentifieAction.async { implicit request =>
    val candidatId = candidatCommandHandler.newId
    val command = InscrireCandidatCommand(
      id = candidatId,
      nom = Nom("perspectives"),
      prenom = Prenom("mickael"),
      email = Email("mickael.perspectives@mail.com"),
      genre = Genre.HOMME
    )
    candidatCommandHandler.handle(command).map { _ =>
      val candidatAuthentifie = CandidatAuthentifie(
        candidatId = command.id,
        nom = command.nom,
        prenom = command.prenom
      )
      Redirect(routes.SaisieCriteresRechercheController.saisieCriteresRecherche())
        .withSession(SessionCandidatAuthentifie.set(candidatAuthentifie, request.session))
        .flashing(request.flash.withCandidatInscrit)
    }
  }

  def deconnexion: Action[AnyContent] = candidatAuthentifieAction { implicit request =>
    Redirect(routes.LandingController.landing())
      .withSession(SessionCandidatAuthentifie.remove(request.session))
  }
}
