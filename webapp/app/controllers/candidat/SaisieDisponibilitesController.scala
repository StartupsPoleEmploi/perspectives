package controllers.candidat

import java.time.LocalDate

import authentification._
import conf.WebAppConfig
import controllers.{AssetsFinder, FormHelpers}
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.commun.infra.play.http.HttpCommandHandler
import fr.poleemploi.perspectives.emailing.domain.EmailingService
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, CandidatSaisieDisponibilitesQuery}
import fr.poleemploi.perspectives.projections.metier.MetierQueryHandler
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc._
import play.filters.csrf.CSRF
import tracking.TrackingService

import scala.concurrent.{ExecutionContext, Future}

class SaisieDisponibilitesController @Inject()(components: ControllerComponents,
                                               implicit val assets: AssetsFinder,
                                               implicit val webAppConfig: WebAppConfig,
                                               messagesAction: MessagesActionBuilder,
                                               candidatCommandHandler: HttpCommandHandler[Candidat],
                                               candidatQueryHandler: CandidatQueryHandler,
                                               metierQueryHandler: MetierQueryHandler,
                                               candidatAuthentifieAction: CandidatAuthentifieAction,
                                               emailingService: EmailingService,
                                               candidatAConnecterSiNonAuthentifieAction: CandidatAConnecterSiNonAuthentifieAction)(implicit exec: ExecutionContext) extends AbstractController(components) {

  def saisieDisponibilites(candidatEnRecherche: Option[Boolean]): Action[AnyContent] = candidatAConnecterSiNonAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      for {
        candidatSaisieDisponibilitesQueryResult <-
          candidatQueryHandler.handle(CandidatSaisieDisponibilitesQuery(candidatAuthentifieRequest.candidatId)).map(Some(_))
      } yield {
        val form = candidatEnRecherche.map(c =>
          SaisieDisponibilitesForm.nouvellesDisponibilites(Some(c))
        ).getOrElse(
          candidatSaisieDisponibilitesQueryResult
            .map(SaisieDisponibilitesForm.fromCandidatDisponibilitesQueryResult)
            .getOrElse(SaisieDisponibilitesForm.nouvellesDisponibilites(None))
        )

        Ok(views.html.candidat.saisieDisponibilites(
          candidatAuthentifie = candidatAuthentifieRequest.candidatAuthentifie,
          jsData = Json.obj(
            "csrfToken" -> CSRF.getToken.map(_.value),
            "disponibilitesFormData" -> form.value
          ),
          gtmDataLayer = TrackingService.buildTrackingCandidat(
            optCandidatAuthentifie = Some(candidatAuthentifieRequest.candidatAuthentifie),
            flash = Some(messagesRequest.flash)
          )
        ))
      }
    }(candidatAuthentifieRequest)
  }

  def modifierDisponibilites: Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      SaisieDisponibilitesForm.form.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
        saisieDisponibilitesForm => {
          val modifierDisponibilitesCommand = buildModifierCandidatCommand(candidatAuthentifieRequest.candidatId, saisieDisponibilitesForm)
          candidatCommandHandler.handle(modifierDisponibilitesCommand).map(_ =>
            Ok(Json.obj("candidatEnRecherche" -> modifierDisponibilitesCommand.candidatEnRecherche)))
        })
    }(candidatAuthentifieRequest)
  }

  def saisieDisponibilitesJVR(candidatEnRecherche: Boolean): Action[AnyContent] = candidatAConnecterSiNonAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      val saisieDisponibilitesForm = SaisieDisponibilitesForm(
        candidatEnRecherche = FormHelpers.optBooleanToString(Some(candidatEnRecherche)),
        disponibiliteConnue = None,
        nbMoisProchaineDisponibilite = None,
        emploiTrouveGracePerspectives = None
      )
      val modifierDisponibilitesCommand = buildModifierCandidatCommand(candidatAuthentifieRequest.candidatId, saisieDisponibilitesForm)

      for {
        _ <- candidatCommandHandler.handle(modifierDisponibilitesCommand)
        _ <- emailingService.mettreAJourDisponibiliteCandidatJVR(candidatAuthentifieRequest.candidatId, candidatEnRecherche)
      } yield {
        Ok(views.html.candidat.saisieDisponibilitesJVR(
          candidatAuthentifie = candidatAuthentifieRequest.candidatAuthentifie,
          jsData = Json.obj(
            "confirmationDispo" -> candidatEnRecherche,
            "confirmationNonDispo" -> !candidatEnRecherche
          ),
          gtmDataLayer = TrackingService.buildTrackingCandidat(
            optCandidatAuthentifie = Some(candidatAuthentifieRequest.candidatAuthentifie),
            flash = Some(messagesRequest.flash)
          )
        ))
      }
    }(candidatAuthentifieRequest)
  }

  private def buildModifierCandidatCommand(candidatId: CandidatId, saisieDisponibilitesForm: SaisieDisponibilitesForm): ModifierDisponibilitesCommand = {
    val candidatEnRecherche = FormHelpers.stringToBoolean(saisieDisponibilitesForm.candidatEnRecherche)
    ModifierDisponibilitesCommand(
      id = candidatId,
      prochaineDisponibilite = if (!candidatEnRecherche && saisieDisponibilitesForm.disponibiliteConnue.exists(FormHelpers.stringToBoolean)) saisieDisponibilitesForm.nbMoisProchaineDisponibilite.map(nbMoisToLocalDate) else None,
      emploiTrouveGracePerspectives = !candidatEnRecherche && saisieDisponibilitesForm.emploiTrouveGracePerspectives.exists(FormHelpers.stringToBoolean),
      candidatEnRecherche = candidatEnRecherche
    )
  }

  private def nbMoisToLocalDate(nbMoisProchaineDisponibilite: Int): LocalDate =
    LocalDate.now().plusMonths(nbMoisProchaineDisponibilite)
}
