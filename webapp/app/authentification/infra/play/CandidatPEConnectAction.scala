package authentification.infra.play

import javax.inject.Inject
import play.api.http.Status.UNAUTHORIZED
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

object SessionCandidatPEConnect {

  private val namespace = "candidat"
  private val idTokenAttribute = s"$namespace.idTokenPEConnect"

  def get(session: Session): Option[String] =
    for {
      idTokenPEConnect <- session.get(idTokenAttribute)
    } yield idTokenPEConnect

  def set(idTokenPEConnect: String,
          session: Session): Session =
    session + (idTokenAttribute -> idTokenPEConnect)

  def remove(session: Session): Session =
    session - idTokenAttribute
}

case class CandidatPEConnectRequest[A](idTokenPEConnect: String,
                                       request: Request[A]) extends WrappedRequest[A](request)

class CandidatPEConnectAction @Inject()(override val parser: BodyParsers.Default)
                                       (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[CandidatPEConnectRequest, AnyContent] {

  override def invokeBlock[A](request: Request[A], block: CandidatPEConnectRequest[A] => Future[Result]): Future[Result] = {
    SessionCandidatPEConnect
      .get(request.session)
      .map(idTokenPEConnect => block(CandidatPEConnectRequest(idTokenPEConnect, request)))
      .getOrElse(Future.successful(Results.Status(UNAUTHORIZED)))
  }
}