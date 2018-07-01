package controllers

import authentification.{AuthenticatedAction, AuthenticatedCandidat}
import conf.WebAppConfig
import fr.poleemploi.eventsourcing.AggregateId
import fr.poleemploi.perspectives.domain.candidat.{CandidatCommandHandler, ModifierCriteresRechercheCommand}
import fr.poleemploi.perspectives.projections.{CandidatQueryHandler, GetCandidatQuery}
import javax.inject.Inject
import play.api.Logger
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SaisieCriteresRechercheController @Inject()(components: ControllerComponents,
                                                  implicit val webAppConfig: WebAppConfig,
                                                  messagesAction: MessagesActionBuilder,
                                                  commandHandler: CandidatCommandHandler,
                                                  queryHandler: CandidatQueryHandler,
                                                  authenticatedAction: AuthenticatedAction) extends AbstractController(components) {

  def saisieCriteresRecherche(): Action[AnyContent] = (authenticatedAction andThen messagesAction).async { implicit request: MessagesRequest[AnyContent] =>
    def booleanToString(boolean: Boolean): String = if (boolean) "true" else "false"

    queryHandler.getCandidat(GetCandidatQuery(
      candidatId = request.session.get("candidatId").get // TODO : aggregateId sale
    )).map(candidatDto => {
      val filledForm = SaisieCriteresRechercheForm.form.fill(
        SaisieCriteresRechercheForm(
          rechercheMetierEvalue = candidatDto.rechercheMetierEvalue.map(booleanToString).getOrElse(""),
          rechercheAutreMetier = candidatDto.rechercheAutreMetier.map(booleanToString).getOrElse(""),
          listeMetiersRecherches = candidatDto.metiersRecherches,
          etreContacteParAgenceInterim = candidatDto.contacteParAgenceInterim.map(booleanToString).getOrElse(""),
          etreContacteParOrganismeFormation = candidatDto.contacteParOrganismeFormation.map(booleanToString).getOrElse(""),
          rayonRecherche = candidatDto.rayonRecherche.getOrElse(0)
        )
      )
      // TODO : accéder au candidat directement depuis la request
      Ok(views.html.saisieCriteresRecherche(filledForm, authenticatedCandidat = AuthenticatedCandidat.buildFromSession(request.session).get))
    })
  }

  def modifierCriteresRecherche(): Action[AnyContent] = (authenticatedAction andThen messagesAction).async { implicit request: MessagesRequest[AnyContent] =>
    def stringToBoolean(string: String): Boolean = if ("true".equalsIgnoreCase(string)) true else false

    SaisieCriteresRechercheForm.form.bindFromRequest.fold(
      formWithErrors => {
        // TODO : accéder au candidat directement depuis la request
        Future.successful(BadRequest(views.html.saisieCriteresRecherche(formWithErrors, authenticatedCandidat = AuthenticatedCandidat.buildFromSession(request.session).get)))
      },
      saisieCriteresRechercheForm => {
        // TODO : aggregateId sale
        val aggregateId = AggregateId(request.session.get("candidatId").get)
        val command = ModifierCriteresRechercheCommand(
          id = aggregateId,
          rechercheMetierEvalue = stringToBoolean(saisieCriteresRechercheForm.rechercheMetierEvalue),
          rechercheAutreMetier = stringToBoolean(saisieCriteresRechercheForm.rechercheAutreMetier),
          listeMetiersRecherches = if (stringToBoolean(saisieCriteresRechercheForm.rechercheAutreMetier)) saisieCriteresRechercheForm.listeMetiersRecherches else Nil,
          etreContacteParOrganismeFormation = stringToBoolean(saisieCriteresRechercheForm.etreContacteParOrganismeFormation),
          etreContacteParAgenceInterim = stringToBoolean(saisieCriteresRechercheForm.etreContacteParAgenceInterim),
          rayonRecherche = saisieCriteresRechercheForm.rayonRecherche
        )
        commandHandler.modifierCriteresRecherche(command)
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
  }
}
