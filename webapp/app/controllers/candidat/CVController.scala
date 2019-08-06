package controllers.candidat

import akka.stream.scaladsl.Source
import akka.util.ByteString
import authentification.infra.play.{CandidatAConnecterSiNonAuthentifieAction, CandidatAuthentifieAction, CandidatAuthentifieRequest}
import conf.WebAppConfig
import controllers.AssetsFinder
import controllers.FlashMessages._
import fr.poleemploi.perspectives.candidat.cv.domain.TypeMedia
import fr.poleemploi.perspectives.candidat.{AjouterCVCommand, CandidatCommandHandler, RemplacerCVCommand}
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.projections.candidat.cv.{DetailsCVCandidatQuery, TelechargerCVCandidatQuery}
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
                             candidatAuthentifieAction: CandidatAuthentifieAction,
                             candidatAConnecterSiNonAuthentifieAction: CandidatAConnecterSiNonAuthentifieAction) extends AbstractController(components) {

  def index: Action[AnyContent] = candidatAConnecterSiNonAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      candidatQueryHandler.handle(DetailsCVCandidatQuery(candidatAuthentifieRequest.candidatId))
        .map(cv => Ok(views.html.candidat.depotCV(
          candidatAuthentifie = candidatAuthentifieRequest.candidatAuthentifie,
          jsData = Json.obj(
            "nouveauCandidat" -> messagesRequest.flash.nouveauCandidat,
            "nomFichier" -> cv.nomFichier,
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
          candidatQueryHandler.handle(DetailsCVCandidatQuery(candidatAuthentifieRequest.candidatId)).flatMap(
            _.cvId.map(cvId => candidatCommandHandler.handle(
              RemplacerCVCommand(id = candidatAuthentifieRequest.candidatId, cvId = cvId, typeMedia = cvForm.typeMedia, path = cvForm.path)
            )).getOrElse(candidatCommandHandler.handle(
              AjouterCVCommand(id = candidatAuthentifieRequest.candidatId, typeMedia = cvForm.typeMedia, path = cvForm.path)
            )).map(_ => NoContent)
          )
      )
    }

  def telecharger(nomFichier: String): Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      candidatQueryHandler.handle(TelechargerCVCandidatQuery(candidatAuthentifieRequest.candidatId))
        .map(cv => {
          val source: Source[ByteString, _] = Source.fromIterator[ByteString](
            () => Iterator.fill(1)(ByteString(cv.data))
          )

          Result(
            header = ResponseHeader(200, Map(
              "Content-Disposition" -> "inline"
            )),
            body = HttpEntity.Streamed(
              data = source,
              contentLength = Some(cv.data.length.toLong),
              contentType = Some(cv.typeMedia.value))
          )
        })
    }(candidatAuthentifieRequest)
  }
}