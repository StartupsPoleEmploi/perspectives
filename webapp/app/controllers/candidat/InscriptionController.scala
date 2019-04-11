package controllers.candidat

import authentification.infra.play.SessionCandidatAuthentifie
import conf.WebAppConfig
import controllers.FlashMessages._
import fr.poleemploi.perspectives.authentification.domain.CandidatAuthentifie
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.commun.domain.{Email, Genre, Nom, Prenom}
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class InscriptionController @Inject()(cc: ControllerComponents,
                                      implicit val webAppConfig: WebAppConfig,
                                      candidatCommandHandler: CandidatCommandHandler,
                                      peConnectController: PEConnectController) extends AbstractController(cc) {

  def inscription(): Action[AnyContent] =
    if (webAppConfig.usePEConnect) {
      peConnectController.inscription()
    } else inscriptionSimple()

  private def inscriptionSimple(): Action[AnyContent] = Action.async { implicit request =>
    val candidatId = candidatCommandHandler.newId
    val command = InscrireCandidatCommand(
      id = candidatId,
      nom = Nom("perspectives"),
      prenom = Prenom("mickael"),
      email = Email("mickael.perspectives@mail.com"),
      genre = Genre.HOMME,
      adresse = Some(Adresse(
        voie = "54 rue René Goscinny",
        codePostal = "85000",
        libelleCommune = "La Roche sur Yon",
        libellePays = "France"
      )),
      statutDemandeurEmploi = Some(StatutDemandeurEmploi.DEMANDEUR_EMPLOI)
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
}
