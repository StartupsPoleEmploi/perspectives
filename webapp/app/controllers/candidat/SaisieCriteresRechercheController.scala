package controllers.candidat

import authentification._
import conf.WebAppConfig
import controllers.FlashMessages._
import controllers.{AssetsFinder, FormHelpers}
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.play.http.HttpCommandHandler
import fr.poleemploi.perspectives.projections.candidat.{CandidatLocalisationQuery, CandidatMetiersValidesQuery, CandidatQueryHandler, CandidatSaisieCriteresRechercheQuery}
import fr.poleemploi.perspectives.projections.metier.{MetierQueryHandler, SecteursActiviteQuery}
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, _}
import play.filters.csrf.CSRF
import tracking.TrackingService

import scala.concurrent.{ExecutionContext, Future}

class SaisieCriteresRechercheController @Inject()(components: ControllerComponents,
                                                  implicit val assets: AssetsFinder,
                                                  implicit val webAppConfig: WebAppConfig,
                                                  messagesAction: MessagesActionBuilder,
                                                  candidatCommandHandler: HttpCommandHandler[Candidat],
                                                  candidatQueryHandler: CandidatQueryHandler,
                                                  metierQueryHandler: MetierQueryHandler,
                                                  candidatAuthentifieAction: CandidatAuthentifieAction,
                                                  candidatAConnecterSiNonAuthentifieAction: CandidatAConnecterSiNonAuthentifieAction)(implicit exec: ExecutionContext) extends AbstractController(components) {

  def saisieCriteresRecherche: Action[AnyContent] = candidatAConnecterSiNonAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      for {
        secteursActivitesQueryResult <- metierQueryHandler.handle(SecteursActiviteQuery)
        candidatSaisieCriteresQueryResult <-
          if (messagesRequest.flash.candidatInscrit) Future.successful(None)
          else candidatQueryHandler.handle(CandidatSaisieCriteresRechercheQuery(candidatAuthentifieRequest.candidatId)).map(Some(_))
      } yield {
        val form = candidatSaisieCriteresQueryResult
          .map(SaisieCriteresRechercheForm.fromCandidatCriteresRechercheQueryResult)
          .getOrElse(SaisieCriteresRechercheForm.nouveauCandidat)

        Ok(views.html.candidat.saisieCriteresRecherche(
          candidatAuthentifie = candidatAuthentifieRequest.candidatAuthentifie,
          jsData = Json.obj(
            "metiersValides" -> candidatSaisieCriteresQueryResult.map(_.metiersValides),
            "secteursActivites" -> secteursActivitesQueryResult.secteursActivites,
            "criteresRechercheFormData" -> form.value,
            "csrfToken" -> CSRF.getToken.map(_.value),
            "algoliaPlacesConfig" -> webAppConfig.algoliaPlacesConfig
          ),
          gtmDataLayer = TrackingService.buildTrackingCandidat(
            optCandidatAuthentifie = Some(candidatAuthentifieRequest.candidatAuthentifie),
            flash = Some(messagesRequest.flash)
          )
        ))
      }
    }(candidatAuthentifieRequest)
  }

  def modifierCriteresRecherche: Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      SaisieCriteresRechercheForm.form.bindFromRequest.fold(
        formWithErrors =>
          for {
            secteursActivitesQueryResult <- metierQueryHandler.handle(SecteursActiviteQuery)
            candidatSaisieCriteresQueryResult <- candidatQueryHandler.handle(CandidatSaisieCriteresRechercheQuery(candidatAuthentifieRequest.candidatId))
          } yield
            BadRequest(views.html.candidat.saisieCriteresRecherche(
              candidatAuthentifie = candidatAuthentifieRequest.candidatAuthentifie,
              jsData = Json.obj(
                "metiersValides" -> candidatSaisieCriteresQueryResult.metiersValides,
                "secteursActivites" -> secteursActivitesQueryResult.secteursActivites,
                "criteresRechercheFormErrors" -> formWithErrors.errorsAsJson,
                "algoliaPlacesConfig" -> webAppConfig.algoliaPlacesConfig
              ),
              gtmDataLayer = TrackingService.buildTrackingCandidat(Some(candidatAuthentifieRequest.candidatAuthentifie))
            )),
        saisieCriteresRechercheForm => {
          val modifierCriteresCommand = buildModifierCandidatCommand(candidatAuthentifieRequest.candidatId, saisieCriteresRechercheForm)

          candidatCommandHandler.handle(modifierCriteresCommand).map(_ =>
            Redirect(routes.RechercheOffreController.index(
              codePostal = Some(modifierCriteresCommand.localisationRecherche.codePostal),
              lieuTravail = Some(modifierCriteresCommand.localisationRecherche.commune),
              rayonRecherche = modifierCriteresCommand.localisationRecherche.rayonRecherche.map(_.value)
            ))
          )
        })
    }(candidatAuthentifieRequest)
  }

  def localisation: Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    candidatQueryHandler.handle(CandidatLocalisationQuery(candidatAuthentifieRequest.candidatId)).map(queryResult =>
      Ok(Json.obj("localisation" -> queryResult))
    )
  }

  def metiersValides: Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    candidatQueryHandler.handle(CandidatMetiersValidesQuery(candidatAuthentifieRequest.candidatId)).map(queryResult =>
      Ok(Json.toJson(queryResult))
    )
  }

  private def buildModifierCandidatCommand(candidatId: CandidatId, saisieCriteresRechercheForm: SaisieCriteresRechercheForm): ModifierCriteresRechercheCommand =
    ModifierCriteresRechercheCommand(
      id = candidatId,
      codesROMEValidesRecherches = saisieCriteresRechercheForm.metiersValidesRecherches.map(CodeROME),
      codesROMERecherches = saisieCriteresRechercheForm.metiersRecherches.map(CodeROME),
      codesDomaineProfessionnelRecherches = saisieCriteresRechercheForm.domainesProfessionnelsRecherches.map(CodeDomaineProfessionnel),
      contactRecruteur = FormHelpers.stringToBoolean(saisieCriteresRechercheForm.contactRecruteur),
      contactFormation = FormHelpers.stringToBoolean(saisieCriteresRechercheForm.contactFormation),
      numeroTelephone = saisieCriteresRechercheForm.numeroTelephone.map(NumeroTelephone(_)),
      localisationRecherche = LocalisationRecherche(
        commune = saisieCriteresRechercheForm.localisation.commune,
        codePostal = saisieCriteresRechercheForm.localisation.codePostal,
        coordonnees = Coordonnees(
          latitude = saisieCriteresRechercheForm.localisation.latitude,
          longitude = saisieCriteresRechercheForm.localisation.longitude
        ),
        rayonRecherche = saisieCriteresRechercheForm.rayonRecherche.flatMap(RayonRecherche.from)
      ),
      tempsTravailRecherche = TempsTravail.from(saisieCriteresRechercheForm.tempsTravail).get
    )
}
