package controllers.candidat

import authentification.infra.play.{CandidatAuthentifieAction, CandidatAuthentifieRequest}
import conf.WebAppConfig
import fr.poleemploi.perspectives.domain.candidat._
import fr.poleemploi.perspectives.domain.{Metier, NumeroTelephone}
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, GetCandidatQuery, GetDetailsCVByCandidat}
import javax.inject.Inject
import play.api.Logger
import play.api.libs.Files
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

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
        detailsCvDto <- candidatQueryHandler.getDetailsCvCandidat(GetDetailsCVByCandidat(candidatAuthentifieRequest.candidatId))
      } yield {
        val filledForm = SaisieCriteresRechercheForm.form.fill(
          SaisieCriteresRechercheForm(
            rechercheMetierEvalue = candidatDto.rechercheMetierEvalue.map(booleanToString).getOrElse(""),
            rechercheAutreMetier = candidatDto.rechercheAutreMetier.map(booleanToString).getOrElse(""),
            metiersRecherches = candidatDto.metiersRecherches.map(_.code),
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

  // La taille est validée au niveau du parser pour refuser la requête et ne pas enregistrer le fichier en cas de dépassement
  def modifierCriteresRecherche(): Action[MultipartFormData[Files.TemporaryFile]] =
    candidatAuthentifieAction.async(parse.multipartFormData(5L * 1000L * 1000L)) { candidatAuthentifieRequest: CandidatAuthentifieRequest[MultipartFormData[Files.TemporaryFile]] =>
      messagesAction.async(parse.multipartFormData) { implicit messagesRequest: MessagesRequest[MultipartFormData[Files.TemporaryFile]] =>
        // TODO
        val detailsCvDto = Await.result(candidatQueryHandler.getDetailsCvCandidat(GetDetailsCVByCandidat(candidatAuthentifieRequest.candidatId)), 5.seconds)
        val candidatAvecCv = detailsCvDto.isDefined
        val cvFourni = messagesRequest.body.file("cv").exists(_.ref.path.toFile.length() > 0)
        val typeMediaValide = messagesRequest.body.file("cv").flatMap(_.contentType).exists(SaisieCriteresRechercheForm.mediaTypesValides.contains)

        val form = if (!cvFourni && !candidatAvecCv) {
          SaisieCriteresRechercheForm.form.withError("cv", "error.required")
        } else if (cvFourni && !typeMediaValide) {
          SaisieCriteresRechercheForm.form.withError("cv", "error.typeMediaInvalide")
        } else SaisieCriteresRechercheForm.form

        form.bindFromRequest.fold(
          formWithErrors => {
            Future.successful(BadRequest(views.html.candidat.saisieCriteresRecherche(
              saisieCriteresRechercheForm = formWithErrors,
              detailsCVDto = detailsCvDto,
              candidatAuthentifie = candidatAuthentifieRequest.candidatAuthentifie)))
          },
          saisieCriteresRechercheForm => {
            val candidatId = CandidatId(candidatAuthentifieRequest.candidatId)
            val modifierCVCommand = buildModifierCvCommand(candidatId, messagesRequest.body.file("cv").get)
            val modifierNumeroTelephoneCommand = buildModifierNumeroTelephoneCommand(candidatId, saisieCriteresRechercheForm)
            val modifierCriteresCommand = buildModifierCriteresRechercheCommand(candidatId, saisieCriteresRechercheForm)

            (for {
              _ <- candidatCommandHandler.modifierCriteresRechercheEtTelephone(modifierCriteresCommand, modifierNumeroTelephoneCommand)
              _ <- candidatCommandHandler.modifierCV(modifierCVCommand)
            } yield ()).map(_ =>
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
      rayonRecherche = saisieCriteresRechercheForm.rayonRecherche
    )
  }

  private def buildModifierNumeroTelephoneCommand(candidatId: CandidatId, saisieCriteresRechercheForm: SaisieCriteresRechercheForm): ModifierNumeroTelephoneCommand =
    ModifierNumeroTelephoneCommand(
      id = candidatId,
      numeroTelephone = NumeroTelephone.from(saisieCriteresRechercheForm.numeroTelephone).get
    )

  private def buildModifierCvCommand(candidatId: CandidatId, cv: MultipartFormData.FilePart[Files.TemporaryFile]): ModifierCVCommand =
    ModifierCVCommand(
      id = candidatId,
      nomFichier = cv.filename,
      typeMedia = cv.contentType.getOrElse(""),
      path = cv.ref.path
    )
}
