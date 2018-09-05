package controllers.candidat

import java.time.LocalDate

import authentification.infra.play.{CandidatAuthentifieAction, CandidatAuthentifieRequest, SessionCandidatAuthentifie}
import conf.WebAppConfig
import controllers.FlashMessages._
import fr.poleemploi.perspectives.authentification.domain.CandidatAuthentifie
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.commun.domain.{CodeROME, Email, Genre}
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class InscriptionController @Inject()(cc: ControllerComponents,
                                      implicit val webappConfig: WebAppConfig,
                                      candidatCommandHandler: CandidatCommandHandler,
                                      candidatAuthentifieAction: CandidatAuthentifieAction,
                                      peConnectController: PEConnectController) extends AbstractController(cc) {

  def inscription(): Action[AnyContent] =
    if (webappConfig.usePEConnect) {
      peConnectController.inscription()
    } else inscriptionSimple()

  private def inscriptionSimple(): Action[AnyContent] = Action.async { implicit request =>
    val candidatId = candidatCommandHandler.newCandidatId
    val command = InscrireCandidatCommand(
      id = candidatId,
      nom = "perspectives",
      prenom = "mickael",
      email = Email("mickael.perspectives@mail.com"),
      genre = Genre.HOMME,
      adresse = Adresse(
        voie = "3 rue des oursons",
        codePostal = "75020",
        libelleCommune = "Paris",
        libellePays = "France"
      ),
      statutDemandeurEmploi = StatutDemandeurEmploi.DEMANDEUR_EMPLOI,
      mrsValidees = List(
        MRSValidee(
          codeROME = CodeROME("B1802"),
          dateEvaluation = LocalDate.now()
        ),
        MRSValidee(
          codeROME = CodeROME("I1307"),
          dateEvaluation = LocalDate.now()
        )
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
        .flashing(request.flash.withCandidatInscrit)
    }
  }

  def confirmationInscription(): Action[AnyContent] = candidatAuthentifieAction.async { implicit candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    Future(Ok(views.html.candidat.confirmationInscription(candidatAuthentifieRequest.candidatAuthentifie)))
  }
}
