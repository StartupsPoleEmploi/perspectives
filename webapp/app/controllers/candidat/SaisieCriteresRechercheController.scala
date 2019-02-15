package controllers.candidat

import authentification.infra.play.{CandidatAuthentifieAction, CandidatAuthentifieRequest}
import conf.WebAppConfig
import controllers.FlashMessages._
import controllers.{AssetsFinder, FormHelpers}
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.commun.domain.{CodeROME, NumeroTelephone, RayonRecherche}
import fr.poleemploi.perspectives.projections.candidat.mrs.MetiersEvaluesNouvelInscritQuery
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, CandidatSaisieCriteresRechercheQuery, CandidatSaisieCriteresRechercheQueryResult}
import fr.poleemploi.perspectives.projections.rechercheCandidat.RechercheCandidatQueryHandler
import javax.inject.Inject
import play.api.libs.Files
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SaisieCriteresRechercheController @Inject()(components: ControllerComponents,
                                                  implicit val assets: AssetsFinder,
                                                  implicit val webAppConfig: WebAppConfig,
                                                  messagesAction: MessagesActionBuilder,
                                                  candidatCommandHandler: CandidatCommandHandler,
                                                  candidatQueryHandler: CandidatQueryHandler,
                                                  rechercheCandidatQueryHandler: RechercheCandidatQueryHandler,
                                                  candidatAuthentifieAction: CandidatAuthentifieAction) extends AbstractController(components) {

  val rayonsRecherche: List[RayonRecherche] = List(
    RayonRecherche.MAX_10,
    RayonRecherche.MAX_30,
    RayonRecherche.MAX_50,
    RayonRecherche.MAX_100
  )

  def saisieCriteresRecherche: Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      for {
        candidatSaisieCriteresRecherche <-
          if (messagesRequest.flash.candidatInscrit) Future.successful(None)
          else candidatQueryHandler.handle(CandidatSaisieCriteresRechercheQuery(candidatAuthentifieRequest.candidatId)).map(Some(_))
        metiersEvaluesCandidat <-
          if (messagesRequest.flash.candidatInscrit) candidatQueryHandler.handle(MetiersEvaluesNouvelInscritQuery(candidatAuthentifieRequest.candidatId)).map(_.metiers)
          else Future(candidatSaisieCriteresRecherche.map(c => c.metiersEvalues).getOrElse(Nil))
      } yield {
        val form = candidatSaisieCriteresRecherche
          .map(SaisieCriteresRechercheForm.fromCandidatCriteresRechercheDto)
          .getOrElse(SaisieCriteresRechercheForm.nouveauCandidat)

        Ok(views.html.candidat.saisieCriteresRecherche(
          saisieCriteresRechercheForm = form,
          candidatSaisieCriteresRecherche = candidatSaisieCriteresRecherche,
          metiersEvaluesCandidat = metiersEvaluesCandidat,
          candidatAuthentifie = candidatAuthentifieRequest.candidatAuthentifie,
          secteursActivites = rechercheCandidatQueryHandler.secteursProposes,
          rayonsRecherche = rayonsRecherche
        ))
      }
    }(candidatAuthentifieRequest)
  }

  def modifierCriteresRecherche: Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      candidatQueryHandler.handle(CandidatSaisieCriteresRechercheQuery(candidatAuthentifieRequest.candidatId))
        .flatMap(candidatSaisieCriteresRecherche => {
          SaisieCriteresRechercheForm.form.bindFromRequest.fold(
            formWithErrors =>
              Future.successful(BadRequest(views.html.candidat.saisieCriteresRecherche(
                saisieCriteresRechercheForm = formWithErrors,
                candidatSaisieCriteresRecherche = Some(candidatSaisieCriteresRecherche),
                metiersEvaluesCandidat = candidatSaisieCriteresRecherche.metiersEvalues,
                candidatAuthentifie = candidatAuthentifieRequest.candidatAuthentifie,
                secteursActivites = rechercheCandidatQueryHandler.secteursProposes,
                rayonsRecherche = rayonsRecherche
              ))),
            saisieCriteresRechercheForm => {
              val modifierCriteresCommand = buildModifierCriteresRechercheCommand(candidatSaisieCriteresRecherche.candidatId, saisieCriteresRechercheForm)

              candidatCommandHandler.handle(modifierCriteresCommand).map(_ =>
                if (saisieCriteresRechercheForm.nouveauCandidat) {
                  Redirect(routes.InscriptionController.confirmationInscription())
                } else {
                  Redirect(routes.RechercheOffreController.index())
                }
              )
            }
          )
        })
    }(candidatAuthentifieRequest)
  }

  def modifierCV: Action[MultipartFormData[Files.TemporaryFile]] =
    candidatAuthentifieAction.async(parse.multipartFormData(CVForm.maxLength)) { implicit candidatAuthentifieRequest: CandidatAuthentifieRequest[MultipartFormData[Files.TemporaryFile]] =>
      CVForm.bindFromMultipart(candidatAuthentifieRequest.body).fold(
        erreur => Future.successful(BadRequest(erreur)),
        cvForm => {
          candidatQueryHandler.handle(CandidatSaisieCriteresRechercheQuery(candidatAuthentifieRequest.candidatId))
            .flatMap(candidat =>
              candidat.cvId
                .map(cvId => candidatCommandHandler.handle(buildRemplacerCvCommand(candidat, cvId, cvForm)))
                .getOrElse(candidatCommandHandler.handle(buildAjouterCvCommand(candidat, cvForm)))
            ).map(_ => NoContent)
        }
      )
    }

  private def buildModifierCriteresRechercheCommand(candidatId: CandidatId, saisieCriteresRechercheForm: SaisieCriteresRechercheForm): ModifierCriteresRechercheCommand =
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

  private def buildAjouterCvCommand(candidat: CandidatSaisieCriteresRechercheQueryResult,
                                    cvForm: CVForm): AjouterCVCommand =
    AjouterCVCommand(
      id = candidat.candidatId,
      nomFichier = s"${candidat.nom.toLowerCase}-${candidat.prenom.toLowerCase}.${TypeMedia.getExtensionFichier(cvForm.typeMedia)}",
      typeMedia = cvForm.typeMedia,
      path = cvForm.path
    )

  private def buildRemplacerCvCommand(candidat: CandidatSaisieCriteresRechercheQueryResult,
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
