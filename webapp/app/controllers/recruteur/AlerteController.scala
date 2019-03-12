package controllers.recruteur

import authentification.infra.play.{RecruteurAuthentifieAction, RecruteurAuthentifieRequest}
import conf.WebAppConfig
import controllers.AssetsFinder
import fr.poleemploi.perspectives.commun.domain.{CodeROME, CodeSecteurActivite, Coordonnees}
import fr.poleemploi.perspectives.recruteur.alerte.domain.{AlerteId, FrequenceAlerte, LocalisationAlerte}
import fr.poleemploi.perspectives.recruteur.{CreerAlerteCommand, RecruteurCommandHandler, SupprimerAlerteCommand}
import javax.inject.{Inject, Singleton}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AlerteController @Inject()(cc: ControllerComponents,
                                            implicit val assets: AssetsFinder,
                                            implicit val webAppConfig: WebAppConfig,
                                            messagesAction: MessagesActionBuilder,
                                            recruteurCommandHandler: RecruteurCommandHandler,
                                            recruteurAuthentifieAction: RecruteurAuthentifieAction) extends AbstractController(cc) {

  def creerAlerte: Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      CreerAlerteForm.form.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
        creerAlerteForm => {
          val alerteId = recruteurCommandHandler.newAlerteId
          recruteurCommandHandler.handle(
            CreerAlerteCommand(
              id = recruteurAuthentifieRequest.recruteurId,
              alerteId = alerteId,
              codeSecteurActivite = creerAlerteForm.secteurActivite.map(CodeSecteurActivite),
              codeROME = creerAlerteForm.metier.map(CodeROME),
              localisation = creerAlerteForm.localisation.map(l => LocalisationAlerte(
                label = l.label,
                coordonnees = Coordonnees(
                  latitude = l.latitude,
                  longitude = l.longitude
                )
              )),
              frequenceAlerte = FrequenceAlerte.frequenceAlerte(creerAlerteForm.frequence).get
            )
          ).map(_ => Created(alerteId.value))
        }
      )
    }(recruteurAuthentifieRequest)
  }

  def supprimerAlerte(alerteId: String): Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      recruteurCommandHandler.handle(SupprimerAlerteCommand(
        id = recruteurAuthentifieRequest.recruteurId,
        alerteId = AlerteId(alerteId)
      )).map(_ => NoContent)
    }(recruteurAuthentifieRequest)
  }
}
