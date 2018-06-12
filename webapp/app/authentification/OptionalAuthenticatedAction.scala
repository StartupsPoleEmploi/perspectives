package authentification

import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class OptionalCandidatRequest[A](authenticatedCandidat: Option[AuthenticatedCandidat],
                                      request: Request[A]) extends WrappedRequest[A](request) {

  def isCandidatAuthentifie: Boolean = authenticatedCandidat.isDefined
}

class OptionalAuthenticatedAction @Inject()(override val parser: BodyParsers.Default)
                                           (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[OptionalCandidatRequest, AnyContent] {

  override def invokeBlock[A](request: Request[A], block: OptionalCandidatRequest[A] => Future[Result]): Future[Result] =
    block(OptionalCandidatRequest(AuthenticatedCandidat.buildFromSession(request.session), request))
}
