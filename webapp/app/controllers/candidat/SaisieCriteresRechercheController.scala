package controllers.candidat

import authentification.infra.play.{CandidatAuthentifieAction, CandidatAuthentifieRequest}
import conf.WebAppConfig
import controllers.FlashMessages._
import controllers.FormHelpers
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.commun.domain.{CodeROME, NumeroTelephone, RayonRecherche}
import fr.poleemploi.perspectives.projections.candidat.{CandidatCriteresRechercheDto, CandidatQueryHandler, CriteresRechercheQuery}
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
        candidat <-
          if (messagesRequest.flash.candidatInscrit) Future.successful(None)
          else candidatQueryHandler.criteresRecherche(CriteresRechercheQuery(candidatAuthentifieRequest.candidatId)).map(Some(_))
      } yield {
        val form = candidat
          .map(SaisieCriteresRechercheForm.fromCandidatCriteresRechercheDto)
          .getOrElse(SaisieCriteresRechercheForm.nouveauCandidat)

        Ok(views.html.candidat.saisieCriteresRecherche(
          saisieCriteresRechercheForm = form,
          candidat = candidat,
          candidatAuthentifie = candidatAuthentifieRequest.candidatAuthentifie,
          secteursActivites = metierQueryHandler.secteursProposesPourRecherche
        ))
      }
    }(candidatAuthentifieRequest)
  }

  def modifierCriteresRecherche: Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      candidatQueryHandler.criteresRecherche(CriteresRechercheQuery(candidatAuthentifieRequest.candidatId))
        .flatMap(candidat => {
          SaisieCriteresRechercheForm.form.bindFromRequest.fold(
            formWithErrors => {
              Future.successful(BadRequest(views.html.candidat.saisieCriteresRecherche(
                saisieCriteresRechercheForm = formWithErrors,
                candidat = Some(candidat),
                candidatAuthentifie = candidatAuthentifieRequest.candidatAuthentifie,
                secteursActivites = metierQueryHandler.secteursProposesPourRecherche
              )))
            },
            saisieCriteresRechercheForm => {
              val modifierCriteresCommand = buildModifierCriteresRechercheCommand(candidat.candidatId, saisieCriteresRechercheForm)

              candidatCommandHandler.modifierCriteresRecherche(modifierCriteresCommand).map(_ =>
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

  def modifierCV: Action[MultipartFormData[Files.TemporaryFile]] =
    candidatAuthentifieAction.async(parse.multipartFormData(CVForm.maxLength)) { implicit candidatAuthentifieRequest: CandidatAuthentifieRequest[MultipartFormData[Files.TemporaryFile]] =>
      CVForm.bindFromMultipart(candidatAuthentifieRequest.body).fold(
        erreur => Future.successful(BadRequest(erreur)),
        cvForm => {
          candidatQueryHandler.criteresRecherche(CriteresRechercheQuery(candidatAuthentifieRequest.candidatId))
            .flatMap(candidat =>
              candidat.cvId
                .map(cvId => candidatCommandHandler.remplacerCV(buildRemplacerCvCommand(candidat, cvId, cvForm)))
                .getOrElse(candidatCommandHandler.ajouterCV(buildAjouterCvCommand(candidat, cvForm)))
            ).map(_ => NoContent)
        }
      )
    }

  private def buildModifierCriteresRechercheCommand(candidatId: CandidatId, saisieCriteresRechercheForm: SaisieCriteresRechercheForm): ModifierCriteresRechercheCommand = {
    ModifierCriteresRechercheCommand(
      id = candidatId,
      rechercheMetierEvalue = FormHelpers.stringToBoolean(saisieCriteresRechercheForm.rechercheMetierEvalue),
      rechercheAutreMetier = FormHelpers.stringToBoolean(saisieCriteresRechercheForm.rechercheAutreMetier),
      metiersRecherches =
        if (FormHelpers.stringToBoolean(saisieCriteresRechercheForm.rechercheAutreMetier))
          saisieCriteresRechercheForm.metiersRecherches.map(CodeROME)
        else Set.empty,
      etreContacteParOrganismeFormation = FormHelpers.stringToBoolean(saisieCriteresRechercheForm.etreContacteParOrganismeFormation),
      etreContacteParAgenceInterim = FormHelpers.stringToBoolean(saisieCriteresRechercheForm.etreContacteParAgenceInterim),
      rayonRecherche = RayonRecherche(saisieCriteresRechercheForm.rayonRecherche),
      numeroTelephone = NumeroTelephone(saisieCriteresRechercheForm.numeroTelephone)
    )
  }

  private def buildAjouterCvCommand(candidat: CandidatCriteresRechercheDto,
                                    cvForm: CVForm): AjouterCVCommand =
    AjouterCVCommand(
      id = candidat.candidatId,
      nomFichier = s"${candidat.nom.toLowerCase}-${candidat.prenom.toLowerCase}.${TypeMedia.getExtensionFichier(cvForm.typeMedia)}",
      typeMedia = cvForm.typeMedia,
      path = cvForm.path
    )

  private def buildRemplacerCvCommand(candidat: CandidatCriteresRechercheDto,
                                      cvId: CVId,
                                      cvForm: CVForm): RemplacerCVCommand =
    RemplacerCVCommand(
      id = candidat.candidatId,
      cvId = cvId,
      nomFichier = s"${candidat.nom.toLowerCase}-${candidat.prenom.toLowerCase}.${TypeMedia.getExtensionFichier(cvForm.typeMedia)}",
      typeMedia = cvForm.typeMedia,
      path = cvForm.path
    )
}
