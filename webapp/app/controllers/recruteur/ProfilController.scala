package controllers.recruteur

import authentification._
import conf.WebAppConfig
import controllers.FlashMessages._
import controllers.{AssetsFinder, FormHelpers}
import fr.poleemploi.perspectives.commun.domain.NumeroTelephone
import fr.poleemploi.perspectives.projections.recruteur.{ProfilRecruteurQuery, RecruteurQueryHandler}
import fr.poleemploi.perspectives.recruteur.{Adresse, _}
import javax.inject.Inject
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc._
import play.filters.csrf.CSRF
import tracking.TrackingService

import scala.concurrent.{ExecutionContext, Future}

class ProfilController @Inject()(components: ControllerComponents,
                                 implicit val assets: AssetsFinder,
                                 implicit val webAppConfig: WebAppConfig,
                                 messagesAction: MessagesActionBuilder,
                                 recruteurCommandHandler: RecruteurCommandHandler,
                                 recruteurQueryHandler: RecruteurQueryHandler,
                                 recruteurAuthentifieAction: RecruteurAuthentifieAction,
                                 recruteurAConnecterSiNonAuthentifieAction: RecruteurAConnecterSiNonAuthentifieAction)(implicit exec: ExecutionContext) extends AbstractController(components) with Logging {

  def modificationProfil: Action[AnyContent] = recruteurAConnecterSiNonAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      for {
        profilRecruteurQueryResult <-
          if (messagesRequest.flash.recruteurInscrit) Future.successful(None)
          else recruteurQueryHandler.handle(ProfilRecruteurQuery(recruteurAuthentifieRequest.recruteurId)).map(Some(_))
      } yield {
        val form = profilRecruteurQueryResult
          .map(ProfilForm.fromProfilRecruteurQueryResult)
          .getOrElse(ProfilForm.nouveauRecruteur)

        Ok(views.html.recruteur.profil(
          recruteurAuthentifie = recruteurAuthentifieRequest.recruteurAuthentifie,
          jsData = Json.obj(
            "profilFormData" -> form.value,
            "csrfToken" -> CSRF.getToken.map(_.value),
            "algoliaPlacesConfig" -> webAppConfig.algoliaPlacesConfig
          ),
          gtmDataLayer = TrackingService.buildTrackingRecruteur(
            optRecruteurAuthentifie = Some(recruteurAuthentifieRequest.recruteurAuthentifie),
            flash = Some(messagesRequest.flash)
          )
        ))
      }
    }(recruteurAuthentifieRequest)
  }

  def modifierProfil: Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      ProfilForm.form.bindFromRequest.fold(
        formWithErrors =>
          Future(BadRequest(views.html.recruteur.profil(
            recruteurAuthentifie = recruteurAuthentifieRequest.recruteurAuthentifie,
            jsData = Json.obj(
              "profilFormErrors" -> formWithErrors.errorsAsJson,
              "profilFormData" -> formWithErrors.data,
              "algoliaPlacesConfig" -> webAppConfig.algoliaPlacesConfig
            ),
            gtmDataLayer = TrackingService.buildTrackingRecruteur(Some(recruteurAuthentifieRequest.recruteurAuthentifie))
          ))),
        profilForm => {
          val command = ModifierProfilCommand(
            id = recruteurAuthentifieRequest.recruteurId,
            raisonSociale = profilForm.raisonSociale,
            typeRecruteur = TypeRecruteur(profilForm.typeRecruteur),
            numeroSiret = NumeroSiret(profilForm.numeroSiret),
            numeroTelephone = NumeroTelephone(profilForm.numeroTelephone),
            contactParCandidats = FormHelpers.stringToBoolean(profilForm.contactParCandidats),
            adresse = Adresse(
              codePostal = profilForm.adresse.codePostal,
              libelleCommune = profilForm.adresse.commune,
              libellePays = profilForm.adresse.pays
            )
          )
          recruteurCommandHandler.handle(command).map(_ =>
            Redirect(routes.RechercheCandidatController.index()).flashing(
              messagesRequest.flash.withTypeRecruteur(command.typeRecruteur)
            )
          )
        }
      )
    }(recruteurAuthentifieRequest)
  }
}
