package controllers.candidat

import java.util.UUID

import authentification.infra.play.SessionCandidatAuthentifie
import conf.WebAppConfig
import fr.poleemploi.perspectives.domain.Genre
import fr.poleemploi.perspectives.domain.authentification.CandidatAuthentifie
import fr.poleemploi.perspectives.domain.candidat.{Adresse, CandidatCommandHandler, CandidatId, InscrireCandidatCommand}
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class InscriptionController @Inject()(cc: ControllerComponents,
                                      webappConfig: WebAppConfig,
                                      candidatCommandHandler: CandidatCommandHandler,
                                      peConnectController: PEConnectController) extends AbstractController(cc) {

  def inscription(): Action[AnyContent] =
    if (webappConfig.usePEConnect) {
      peConnectController.inscription()
    } else inscriptionSimple()

  private def inscriptionSimple(): Action[AnyContent] = Action.async { implicit request =>
    val candidatId = CandidatId(UUID.randomUUID().toString)
    val command = InscrireCandidatCommand(
      id = candidatId,
      nom = "perspectives",
      prenom = "mickael",
      email = "mickael.perspectives@mail.com",
      genre = Genre.HOMME,
      adresse = Adresse(
        voie = "3 rue des oursons",
        codePostal = "75020",
        libelleCommune = "Paris",
        libellePays = "France"
      )
    )
    candidatCommandHandler.inscrire(command).map { _ =>
      val candidatAuthentifie = CandidatAuthentifie(
        candidatId = command.id,
        nom = command.nom,
        prenom = command.prenom
      )
      Redirect(routes.SaisieCriteresRechercheController.saisieCriteresRecherche())
        .withSession(SessionCandidatAuthentifie.set(candidatAuthentifie, request.session))
    }
  }
}
