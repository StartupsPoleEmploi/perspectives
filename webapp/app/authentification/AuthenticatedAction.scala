package authentification

import javax.inject.Inject
import play.api.http.Status.UNAUTHORIZED
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class CandidatRequest[A](authenticatedCandidat: AuthenticatedCandidat,
                              request: Request[A]) extends WrappedRequest[A](request) {

  def idTokenPEConnect: Option[String] = authenticatedCandidat.idTokenPEConnect
}

class AuthenticatedAction @Inject()(override val parser: BodyParsers.Default)
                                   (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[CandidatRequest, AnyContent] {

  override def invokeBlock[A](request: Request[A], block: CandidatRequest[A] => Future[Result]): Future[Result] = {
    AuthenticatedCandidat
      .buildFromSession(request.session)
      .map(candidat => block(CandidatRequest(candidat, request)))
      .getOrElse(Future.successful(Results.Status(UNAUTHORIZED)))
  }
}
