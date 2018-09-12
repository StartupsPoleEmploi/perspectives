package controllers.recruteur

import authentification.infra.play.{RecruteurAuthentifieAction, RecruteurAuthentifieRequest}
import conf.WebAppConfig
import controllers.FlashMessages._
import controllers.FormHelpers
import fr.poleemploi.perspectives.commun.domain.NumeroTelephone
import fr.poleemploi.perspectives.projections.recruteur.{ProfilRecruteurQuery, RecruteurQueryHandler}
import fr.poleemploi.perspectives.recruteur._
import javax.inject.Inject
import play.api.Logger
import play.api.data.Form
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ProfilController @Inject()(components: ControllerComponents,
                                 implicit val webAppConfig: WebAppConfig,
                                 messagesAction: MessagesActionBuilder,
                                 recruteurCommandHandler: RecruteurCommandHandler,
                                 recruteurQueryHandler: RecruteurQueryHandler,
                                 recruteurAuthentifieAction: RecruteurAuthentifieAction) extends AbstractController(components) {

  def modificationProfil(): Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      val form: Future[Form[ProfilForm]] =
        if (messagesRequest.flash.recruteurInscrit) {
          Future.successful(ProfilForm.nouveauRecruteur)
        } else {
          recruteurQueryHandler
            .profilRecruteur(ProfilRecruteurQuery(recruteurId = recruteurAuthentifieRequest.recruteurId))
            .map(ProfilForm.fromProfilRecruteur)
        }

      form.map(f => Ok(views.html.recruteur.profil(f, recruteurAuthentifie = recruteurAuthentifieRequest.recruteurAuthentifie)))
    }(recruteurAuthentifieRequest)
  }

  def modifierProfil(): Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      ProfilForm.form.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(views.html.recruteur.profil(formWithErrors, recruteurAuthentifie = recruteurAuthentifieRequest.recruteurAuthentifie)))
        },
        inscriptionForm => {
          val command = ModifierProfilCommand(
            id = recruteurAuthentifieRequest.recruteurId,
            raisonSociale = inscriptionForm.raisonSociale,
            typeRecruteur = TypeRecruteur(inscriptionForm.typeRecruteur),
            numeroSiret = NumeroSiret(inscriptionForm.numeroSiret),
            numeroTelephone = NumeroTelephone(inscriptionForm.numeroTelephone),
            contactParCandidats = FormHelpers.stringToBoolean(inscriptionForm.contactParCandidats)
          )
          recruteurCommandHandler.modifierProfil(command)
            .map(_ =>
              Redirect(routes.MatchingController.index()).flashing(
                messagesRequest.flash.withTypeRecruteur(command.typeRecruteur)
              )
            ).recoverWith {
            case t: Throwable =>
              Logger.error("Erreur lors de l'enregistrement de l'inscription", t)
              Future(Redirect(routes.LandingController.landing()).flashing(
                messagesRequest.flash.withMessageErreur("Une erreur s'est produite lors de l'enregistrement, veuillez réessayer ultérieurement")
              ))
          }
        }
      )
    }(recruteurAuthentifieRequest)
  }
}