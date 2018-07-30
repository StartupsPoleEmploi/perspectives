package controllers.candidat

import authentification.infra.play.{CandidatAuthentifieAction, CandidatAuthentifieRequest}
import conf.WebAppConfig
import fr.poleemploi.perspectives.domain.candidat._
import fr.poleemploi.perspectives.domain.candidat.cv.CVId
import fr.poleemploi.perspectives.domain.{Metier, NumeroTelephone}
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, FindDetailsCVByCandidatQuery, GetCandidatQuery}
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
                                                  candidatAuthentifieAction: CandidatAuthentifieAction) extends AbstractController(components) {

  def saisieCriteresRecherche(): Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      def booleanToString(boolean: Boolean): String = if (boolean) "true" else "false"

      for {
        candidatDto <- candidatQueryHandler.getCandidat(GetCandidatQuery(candidatAuthentifieRequest.candidatId))
        detailsCvDto <- candidatQueryHandler.findDetailsCvByCandidat(FindDetailsCVByCandidatQuery(candidatAuthentifieRequest.candidatId))
      } yield {
        val filledForm = SaisieCriteresRechercheForm.form.fill(
          SaisieCriteresRechercheForm(
            rechercheMetierEvalue = candidatDto.rechercheMetierEvalue.map(booleanToString).getOrElse(""),
            rechercheAutreMetier = candidatDto.rechercheAutreMetier.map(booleanToString).getOrElse(""),
            metiersRecherches = candidatDto.metiersRecherches.map(_.value),
            etreContacteParAgenceInterim = candidatDto.contacteParAgenceInterim.map(booleanToString).getOrElse(""),
            etreContacteParOrganismeFormation = candidatDto.contacteParOrganismeFormation.map(booleanToString).getOrElse(""),
            rayonRecherche = candidatDto.rayonRecherche.getOrElse(0),
            numeroTelephone = candidatDto.numeroTelephone.map(_.value).getOrElse("")
          )
        )
        Ok(views.html.candidat.saisieCriteresRecherche(
          saisieCriteresRechercheForm = filledForm,
          detailsCVDto = detailsCvDto,
          candidatAuthentifie = candidatAuthentifieRequest.candidatAuthentifie)
        )
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

        candidatQueryHandler.findDetailsCvByCandidat(FindDetailsCVByCandidatQuery(candidatAuthentifieRequest.candidatId))
          .flatMap(detailsCvDto => {
            form.bindFromRequest.fold(
              formWithErrors => {
                Future.successful(BadRequest(views.html.candidat.saisieCriteresRecherche(
                  saisieCriteresRechercheForm = formWithErrors,
                  detailsCVDto = detailsCvDto,
                  candidatAuthentifie = candidatAuthentifieRequest.candidatAuthentifie)
                ))
              },
              saisieCriteresRechercheForm => {
                val candidatId = candidatAuthentifieRequest.candidatId
                val modifierCriteresCommand = buildModifierCriteresRechercheCommand(candidatId, saisieCriteresRechercheForm)

                (for {
                  _ <- candidatCommandHandler.modifierCriteresRecherche(modifierCriteresCommand)
                  _ <- cv.map(cv =>
                    detailsCvDto
                      .map(detailsCv => candidatCommandHandler.remplacerCV(buildRemplacerCvCommand(candidatId, detailsCv.id, cv)))
                      .getOrElse(candidatCommandHandler.ajouterCV(buildAjouterCvCommand(candidatId, cv)))
                  ) getOrElse Future.successful(())
                } yield ()).map(_ =>
                  Redirect(routes.LandingController.landing()).flashing(
                    ("message_succes", "Merci, vos criteres ont bien été pris en compte")
                  ))
              }
            )
          }).recoverWith {
          case t: Throwable =>
            Logger.error("Erreur lors de l'enregistrement des critères", t)
            Future(Redirect(routes.LandingController.landing()).flashing(
              ("message_erreur", "Une erreur s'est produite lors de l'enregistrement, veuillez réessayer ultérieurement")
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
          saisieCriteresRechercheForm.metiersRecherches.flatMap(Metier.from)
        else Set.empty,
      etreContacteParOrganismeFormation = stringToBoolean(saisieCriteresRechercheForm.etreContacteParOrganismeFormation),
      etreContacteParAgenceInterim = stringToBoolean(saisieCriteresRechercheForm.etreContacteParAgenceInterim),
      rayonRecherche = saisieCriteresRechercheForm.rayonRecherche,
      numeroTelephone = NumeroTelephone.from(saisieCriteresRechercheForm.numeroTelephone).get
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
