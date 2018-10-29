package controllers.candidat

import akka.stream.scaladsl.Source
import akka.util.ByteString
import authentification.infra.play.{CandidatAuthentifieAction, CandidatAuthentifieRequest}
import conf.WebAppConfig
import fr.poleemploi.perspectives.candidat.CandidatCommandHandler
import fr.poleemploi.perspectives.projections.candidat.CandidatQueryHandler
import fr.poleemploi.perspectives.projections.candidat.cv.CVCandidatQuery
import javax.inject.Inject
import play.api.http.HttpEntity
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

class CVController @Inject()(components: ControllerComponents,
                             implicit val webAppConfig: WebAppConfig,
                             messagesAction: MessagesActionBuilder,
                             candidatCommandHandler: CandidatCommandHandler,
                             candidatQueryHandler: CandidatQueryHandler,
                             candidatAuthentifieAction: CandidatAuthentifieAction) extends AbstractController(components) {

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

}
