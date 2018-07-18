package controllers.candidat

import authentification.infra.play.{CandidatAuthentifieAction, CandidatAuthentifieRequest}
import conf.WebAppConfig
import fr.poleemploi.perspectives.domain.Metier
import fr.poleemploi.perspectives.domain.candidat.{CandidatCommandHandler, CandidatId, ModifierCriteresRechercheCommand}
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, GetCandidatQuery}
import javax.inject.Inject
import play.api.Logger
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SaisieCriteresRechercheController @Inject()(components: ControllerComponents,
                                                  implicit val webAppConfig: WebAppConfig,
                                                  messagesAction: MessagesActionBuilder,
                                                  candidatCommandHandler: CandidatCommandHandler,
                                                  candidatQueryHandler: CandidatQueryHandler,
                                                  candidatAuthentifieAction: CandidatAuthentifieAction) extends AbstractController(components) {

  def saisieCriteresRecherche(): Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      def booleanToString(boolean: Boolean): String = if (boolean) "true" else "false"

      candidatQueryHandler.getCandidat(GetCandidatQuery(
        candidatId = candidatAuthentifieRequest.candidatId
      )).map(candidatDto => {
        val filledForm = SaisieCriteresRechercheForm.form.fill(
          SaisieCriteresRechercheForm(
            rechercheMetierEvalue = candidatDto.rechercheMetierEvalue.map(booleanToString).getOrElse(""),
            rechercheAutreMetier = candidatDto.rechercheAutreMetier.map(booleanToString).getOrElse(""),
            metiersRecherches = candidatDto.metiersRecherches.map(_.code),
            etreContacteParAgenceInterim = candidatDto.contacteParAgenceInterim.map(booleanToString).getOrElse(""),
            etreContacteParOrganismeFormation = candidatDto.contacteParOrganismeFormation.map(booleanToString).getOrElse(""),
            rayonRecherche = candidatDto.rayonRecherche.getOrElse(0)
          )
        )
        Ok(views.html.candidat.saisieCriteresRecherche(filledForm, candidatAuthentifie = candidatAuthentifieRequest.candidatAuthentifie))
      })
    }(candidatAuthentifieRequest)
  }

  def modifierCriteresRecherche(): Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      def stringToBoolean(string: String): Boolean = if ("true".equalsIgnoreCase(string)) true else false

      SaisieCriteresRechercheForm.form.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(views.html.candidat.saisieCriteresRecherche(formWithErrors, candidatAuthentifie = candidatAuthentifieRequest.candidatAuthentifie)))
        },
        saisieCriteresRechercheForm => {
          val candidatId = CandidatId(candidatAuthentifieRequest.candidatId)
          val command = ModifierCriteresRechercheCommand(
            id = candidatId,
            rechercheMetierEvalue = stringToBoolean(saisieCriteresRechercheForm.rechercheMetierEvalue),
            rechercheAutreMetier = stringToBoolean(saisieCriteresRechercheForm.rechercheAutreMetier),
            metiersRecherches =
              if (stringToBoolean(saisieCriteresRechercheForm.rechercheAutreMetier))
                saisieCriteresRechercheForm.metiersRecherches.flatMap(Metier.from)
              else Set.empty,
            etreContacteParOrganismeFormation = stringToBoolean(saisieCriteresRechercheForm.etreContacteParOrganismeFormation),
            etreContacteParAgenceInterim = stringToBoolean(saisieCriteresRechercheForm.etreContacteParAgenceInterim),
            rayonRecherche = saisieCriteresRechercheForm.rayonRecherche
          )
          candidatCommandHandler.modifierCriteresRecherche(command)
            .map(_ =>
              Redirect(routes.LandingController.landing()).flashing(
                ("message_succes", "Merci, vos criteres ont bien été pris en compte")
              ))
            .recoverWith {
              case t: Throwable =>
                Logger.error("Erreur lors de l'enregistrement des critères", t)
                Future(Redirect(routes.LandingController.landing()).flashing(
                  ("message_erreur", "Une erreur s'est produite lors de l'enregistrement, veuillez réessayer ultérieurement")
                ))
            }
        }
      )
    }(candidatAuthentifieRequest)
  }
}
