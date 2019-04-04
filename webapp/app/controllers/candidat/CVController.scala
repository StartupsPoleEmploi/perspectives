package controllers.candidat

import akka.stream.scaladsl.Source
import akka.util.ByteString
import authentification.infra.play.{CandidatAuthentifieAction, CandidatAuthentifieRequest}
import conf.WebAppConfig
import controllers.FlashMessages._
import controllers.AssetsFinder
import fr.poleemploi.perspectives.candidat.{AjouterCVCommand, CandidatCommandHandler, RemplacerCVCommand}
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.projections.candidat.cv.CVCandidatQuery
import javax.inject.Inject
import play.api.http.HttpEntity
import play.api.libs.Files
import play.api.libs.json.Json
import play.api.mvc._
import play.filters.csrf.CSRF

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CVController @Inject()(components: ControllerComponents,
                             implicit val assets: AssetsFinder,
                             implicit val webAppConfig: WebAppConfig,
                             messagesAction: MessagesActionBuilder,
                             candidatCommandHandler: CandidatCommandHandler,
                             candidatQueryHandler: CandidatQueryHandler,
                             candidatAuthentifieAction: CandidatAuthentifieAction) extends AbstractController(components) {

  def index: Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      candidatQueryHandler.handle(CandidatDepotCVQuery(candidatAuthentifieRequest.candidatId))
        .map(candidatDepotCVQueryResult => Ok(views.html.candidat.depotCV(
          candidatAuthentifie = candidatAuthentifieRequest.candidatAuthentifie,
          jsData = Json.obj(
            "nouveauCandidat" -> messagesRequest.flash.candidatInscrit,
            "avecCV" -> candidatDepotCVQueryResult.cvId.isDefined,
            "typesMediasValides" -> TypeMedia.typesMediasCV,
            "extensionsValides" -> TypeMedia.extensionsCV,
            "tailleMaxInBytes" -> CVForm.maxLengthInBytes,
            "tailleMaxLabel" -> CVForm.maxLengthLabel,
            "csrfToken" -> CSRF.getToken.map(_.value)
          )
        )))
    }(candidatAuthentifieRequest)
  }

  def modifierCV: Action[MultipartFormData[Files.TemporaryFile]] =
    candidatAuthentifieAction.async(parse.multipartFormData(CVForm.maxLengthInBytes)) { implicit candidatAuthentifieRequest: CandidatAuthentifieRequest[MultipartFormData[Files.TemporaryFile]] =>
      CVForm.bindFromMultipart(candidatAuthentifieRequest.body).fold(
        erreur => Future.successful(BadRequest(erreur)),
        cvForm =>
          candidatQueryHandler.handle(CandidatDepotCVQuery(candidatAuthentifieRequest.candidatId))
            .flatMap(candidat =>
              candidat.cvId
                .map(cvId => candidatCommandHandler.handle(buildRemplacerCvCommand(candidat, cvId, cvForm)))
                .getOrElse(candidatCommandHandler.handle(buildAjouterCvCommand(candidat, cvForm)))
            ).map(_ => NoContent)
      )
    }

  def telecharger(nomFichier: String): Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      candidatQueryHandler.handle(CVCandidatQuery(candidatAuthentifieRequest.candidatId))
        .map(cvCandidat => {
          val source: Source[ByteString, _] = Source.fromIterator[ByteString](
            () => Iterator.fill(1)(ByteString(cvCandidat.cv.data))
          )

          Result(
            header = ResponseHeader(200, Map(
              "Content-Disposition" -> "inline"
            )),
            body = HttpEntity.Streamed(
              data = source,
              contentLength = Some(cvCandidat.cv.data.length.toLong),
              contentType = Some(cvCandidat.cv.typeMedia.value))
          )
        })
    }(candidatAuthentifieRequest)
  }

  private def buildAjouterCvCommand(candidat: CandidatDepotCVQueryResult,
                                    cvForm: CVForm): AjouterCVCommand =
    AjouterCVCommand(
      id = candidat.candidatId,
      typeMedia = cvForm.typeMedia,
      path = cvForm.path
    )

  private def buildRemplacerCvCommand(candidat: CandidatDepotCVQueryResult,
                                      cvId: CVId,
                                      cvForm: CVForm): RemplacerCVCommand =
    RemplacerCVCommand(
      id = candidat.candidatId,
      cvId = cvId,
      typeMedia = cvForm.typeMedia,
      path = cvForm.path
    )

}
