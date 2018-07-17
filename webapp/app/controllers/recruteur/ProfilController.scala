package controllers.recruteur

import authentification.infra.play.{RecruteurAuthentifieAction, RecruteurAuthentifieRequest}
import conf.WebAppConfig
import fr.poleemploi.eventsourcing.AggregateId
import fr.poleemploi.perspectives.domain.NumeroTelephone
import fr.poleemploi.perspectives.domain.recruteur.{ModifierProfilCommand, NumeroSiret, RecruteurCommandHandler, TypeRecruteur}
import fr.poleemploi.perspectives.projections.recruteur.{GetRecruteurQuery, RecruteurQueryHandler}
import javax.inject.Inject
import play.api.Logger
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

      recruteurQueryHandler.getRecruteur(GetRecruteurQuery(
        recruteurId = recruteurAuthentifieRequest.recruteurId
      )).map(recruteurDto => {
        val filledForm = ProfilForm.form.fill(
          ProfilForm(
            typeRecruteur = recruteurDto.typeRecruteur.map(_.code).getOrElse(""),
            raisonSociale = recruteurDto.raisonSociale.getOrElse(""),
            numeroSiret = recruteurDto.numeroSiret.map(_.value).getOrElse(""),
            numeroTelephone = recruteurDto.numeroTelephone.map(_.value).getOrElse(""),
            contactParCandidats = recruteurDto.contactParCandidats.map(booleanToString).getOrElse("")
          )
        )
        Ok(views.html.recruteur.profil(filledForm, recruteurAuthentifie = recruteurAuthentifieRequest.recruteurAuthentifie))
      })
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
          Future.successful(NoContent)
          val aggregateId = AggregateId(recruteurAuthentifieRequest.recruteurId)
          val command = ModifierProfilCommand(
            id = aggregateId,
            raisonSociale = inscriptionForm.raisonSociale,
            typeRecruteur = TypeRecruteur.from(inscriptionForm.typeRecruteur).get,
            numeroSiret = NumeroSiret.from(inscriptionForm.numeroSiret).get,
            numeroTelephone = NumeroTelephone.from(inscriptionForm.numeroTelephone).get,
            contactParCandidats = stringToBoolean(inscriptionForm.contactParCandidats)
          )
          recruteurCommandHandler.modifierProfil(command)
            .map(_ =>
              Redirect(routes.LandingController.landing()).flashing(
                ("message_succes", "Merci, votre inscription a bien été prise en compte")
              ))
            .recoverWith {
              case t: Throwable =>
                Logger.error("Erreur lors de l'enregistrement de l'inscription", t)
                Future(Redirect(routes.LandingController.landing()).flashing(
                  ("message_erreur", "Une erreur s'est produite lors de l'enregistrement, veuillez réessayer ultérieurement")
                ))
            }
        }
      )
    }(recruteurAuthentifieRequest)
  }
}