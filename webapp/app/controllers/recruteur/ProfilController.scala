package controllers.recruteur

import authentification.infra.play.{RecruteurAuthentifieAction, RecruteurAuthentifieRequest}
import conf.WebAppConfig
import controllers.FlashMessages._
import fr.poleemploi.perspectives.domain.NumeroTelephone
import fr.poleemploi.perspectives.domain.recruteur._
import fr.poleemploi.perspectives.projections.recruteur.{GetRecruteurQuery, RecruteurQueryHandler}
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
      def booleanToString(boolean: Boolean): String = if (boolean) "true" else "false"

      val form: Future[Form[ProfilForm]] =
        if (messagesRequest.flash.recruteurInscrit) {
          Future.successful(ProfilForm.nouveauRecruteur)
        } else {
          recruteurQueryHandler.getRecruteur(GetRecruteurQuery(recruteurId = recruteurAuthentifieRequest.recruteurId)).map(recruteurDto =>
            ProfilForm.form.fill(
              ProfilForm(
                nouveauRecruteur = false,
                typeRecruteur = recruteurDto.typeRecruteur.map(_.value).getOrElse(""),
                raisonSociale = recruteurDto.raisonSociale.getOrElse(""),
                numeroSiret = recruteurDto.numeroSiret.map(_.value).getOrElse(""),
                numeroTelephone = recruteurDto.numeroTelephone.map(_.value).getOrElse(""),
                contactParCandidats = recruteurDto.contactParCandidats.map(booleanToString).getOrElse("")
              )
            ))
        }

      form.map(f => Ok(views.html.recruteur.profil(f, recruteurAuthentifie = recruteurAuthentifieRequest.recruteurAuthentifie)))
    }(recruteurAuthentifieRequest)
  }

  def modifierProfil(): Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      def stringToBoolean(string: String): Boolean = if ("true".equalsIgnoreCase(string)) true else false

      ProfilForm.form.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(views.html.recruteur.profil(formWithErrors, recruteurAuthentifie = recruteurAuthentifieRequest.recruteurAuthentifie)))
        },
        inscriptionForm => {
          val command = ModifierProfilCommand(
            id = recruteurAuthentifieRequest.recruteurId,
            raisonSociale = inscriptionForm.raisonSociale,
            typeRecruteur = TypeRecruteur.from(inscriptionForm.typeRecruteur).get,
            numeroSiret = NumeroSiret.from(inscriptionForm.numeroSiret).get,
            numeroTelephone = NumeroTelephone.from(inscriptionForm.numeroTelephone).get,
            contactParCandidats = stringToBoolean(inscriptionForm.contactParCandidats)
          )
          recruteurCommandHandler.modifierProfil(command)
            .map(_ =>
              if (inscriptionForm.nouveauRecruteur) {
                Redirect(routes.InscriptionController.confirmationInscription())
              } else {
                Redirect(routes.LandingController.landing()).flashing(
                  messagesRequest.flash.withMessageSucces("Votre profil a bien été modifié")
                )
              }
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