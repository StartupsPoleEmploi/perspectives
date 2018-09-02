package controllers.candidat

import authentification.infra.play.{CandidatAuthentifieAction, CandidatAuthentifieRequest}
import conf.WebAppConfig
import controllers.FlashMessages._
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.cv.domain.CVId
import fr.poleemploi.perspectives.commun.domain.{CodeROME, NumeroTelephone, RayonRecherche}
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, GetCandidatQuery}
import fr.poleemploi.perspectives.projections.metier.MetierQueryHandler
import javax.inject.Inject
import play.api.Logger
import play.api.libs.Files
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SaisieCriteresRechercheController @Inject()(components: ControllerComponents,
                                                  implicit val webAppConfig: WebAppConfig,
                                                  messagesAction: MessagesActionBuilder,
                                                  candidatCommandHandler: CandidatCommandHandler,
                                                  candidatQueryHandler: CandidatQueryHandler,
                                                  metierQueryHandler: MetierQueryHandler,
                                                  candidatAuthentifieAction: CandidatAuthentifieAction) extends AbstractController(components) {

  def saisieCriteresRecherche(): Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      for {
        candidatDto <-
          if (messagesRequest.flash.candidatInscrit) Future.successful(None)
          else candidatQueryHandler.getCandidat(GetCandidatQuery(candidatAuthentifieRequest.candidatId)).map(Some(_))
      } yield {
        val form = candidatDto
          .map(SaisieCriteresRechercheForm.fromCandidat)
          .getOrElse(SaisieCriteresRechercheForm.nouveauCandidat)

        Ok(views.html.candidat.saisieCriteresRecherche(
          saisieCriteresRechercheForm = form,
          candidatDto = candidatDto,
          candidatAuthentifie = candidatAuthentifieRequest.candidatAuthentifie,
          secteursActivites = metierQueryHandler.secteursProposesPourRecherche
        ))
      }
    }(candidatAuthentifieRequest)
  }

  def modifierCriteresRecherche(): Action[MultipartFormData[Files.TemporaryFile]] =
    candidatAuthentifieAction.async(parse.multipartFormData(5L * 1024 * 1024)) { candidatAuthentifieRequest: CandidatAuthentifieRequest[MultipartFormData[Files.TemporaryFile]] =>
      messagesAction.async(parse.multipartFormData) { implicit messagesRequest: MessagesRequest[MultipartFormData[Files.TemporaryFile]] =>
        val cv = messagesRequest.body.file("cv").filter(_.ref.path.toFile.length() > 0)
        val typeMediaValide = messagesRequest.body.file("cv").flatMap(_.contentType).exists(SaisieCriteresRechercheForm.mediaTypesValides.contains)

        val form = if (cv.isDefined && !typeMediaValide) {
          SaisieCriteresRechercheForm.form.withError("cv", "error.typeMediaInvalide")
        } else SaisieCriteresRechercheForm.form

        candidatQueryHandler.getCandidat(GetCandidatQuery(candidatAuthentifieRequest.candidatId))
          .flatMap(candidatDto => {
            form.bindFromRequest.fold(
              formWithErrors => {
                Future.successful(BadRequest(views.html.candidat.saisieCriteresRecherche(
                  saisieCriteresRechercheForm = formWithErrors,
                  candidatDto = Some(candidatDto),
                  candidatAuthentifie = candidatAuthentifieRequest.candidatAuthentifie,
                  secteursActivites = metierQueryHandler.secteursProposesPourRecherche
                )))
              },
              saisieCriteresRechercheForm => {
                val candidatId = candidatAuthentifieRequest.candidatId
                val modifierCriteresCommand = buildModifierCriteresRechercheCommand(candidatId, saisieCriteresRechercheForm)

                (for {
                  _ <- candidatCommandHandler.modifierCriteresRecherche(modifierCriteresCommand)
                  _ <- cv.map(cv =>
                    candidatDto.cvId
                      .map(cvId => candidatCommandHandler.remplacerCV(buildRemplacerCvCommand(candidatId, cvId, cv)))
                      .getOrElse(candidatCommandHandler.ajouterCV(buildAjouterCvCommand(candidatId, cv)))
                  ) getOrElse Future.successful(())
                } yield ()).map(_ =>
                  if (saisieCriteresRechercheForm.nouveauCandidat) {
                    Redirect(routes.InscriptionController.confirmationInscription())
                  } else {
                    Redirect(routes.LandingController.landing()).flashing(
                      messagesRequest.flash.withMessageSucces("Merci, vos criteres ont bien été pris en compte")
                    )
                  }
                )
              }
            )
          }).recoverWith {
          case t: Throwable =>
            Logger.error("Erreur lors de l'enregistrement des critères", t)
            Future(Redirect(routes.LandingController.landing()).flashing(
              messagesRequest.flash.withMessageErreur("Une erreur s'est produite lors de l'enregistrement, veuillez réessayer ultérieurement")
            ))
        }
      }(candidatAuthentifieRequest)
    }

  private def buildModifierCriteresRechercheCommand(candidatId: CandidatId, saisieCriteresRechercheForm: SaisieCriteresRechercheForm): ModifierCriteresRechercheCommand = {
    def stringToBoolean(string: String): Boolean = if ("true".equalsIgnoreCase(string)) true else false

    ModifierCriteresRechercheCommand(
      id = candidatId,
      rechercheMetierEvalue = stringToBoolean(saisieCriteresRechercheForm.rechercheMetierEvalue),
      rechercheAutreMetier = stringToBoolean(saisieCriteresRechercheForm.rechercheAutreMetier),
      metiersRecherches =
        if (stringToBoolean(saisieCriteresRechercheForm.rechercheAutreMetier))
          saisieCriteresRechercheForm.metiersRecherches.map(CodeROME)
        else Set.empty,
      etreContacteParOrganismeFormation = stringToBoolean(saisieCriteresRechercheForm.etreContacteParOrganismeFormation),
      etreContacteParAgenceInterim = stringToBoolean(saisieCriteresRechercheForm.etreContacteParAgenceInterim),
      rayonRecherche = RayonRecherche(saisieCriteresRechercheForm.rayonRecherche),
      numeroTelephone = NumeroTelephone(saisieCriteresRechercheForm.numeroTelephone)
    )
  }

  private def buildAjouterCvCommand(candidatId: CandidatId,
                                    cv: MultipartFormData.FilePart[Files.TemporaryFile]): AjouterCVCommand =
    AjouterCVCommand(
      id = candidatId,
      nomFichier = cv.filename,
      typeMedia = cv.contentType.getOrElse(""),
      path = cv.ref.path
    )

  private def buildRemplacerCvCommand(candidatId: CandidatId,
                                      cvId: CVId,
                                      cv: MultipartFormData.FilePart[Files.TemporaryFile]): RemplacerCVCommand =
    RemplacerCVCommand(
      id = candidatId,
      cvId = cvId,
      nomFichier = cv.filename,
      typeMedia = cv.contentType.getOrElse(""),
      path = cv.ref.path
    )
}
