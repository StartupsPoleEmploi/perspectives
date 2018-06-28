package authentification.infra.play

import javax.inject.Inject
import play.api.http.Status.UNAUTHORIZED
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

object SessionRecruteurPEConnect {

  private val namespace = "recruteur"
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

case class RecruteurPEConnectRequest[A](idTokenPEConnect: String,
                                        request: Request[A]) extends WrappedRequest[A](request)

class RecruteurPEConnectAction @Inject()(override val parser: BodyParsers.Default)
                                        (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[RecruteurPEConnectRequest, AnyContent] {

  override def invokeBlock[A](request: Request[A], block: RecruteurPEConnectRequest[A] => Future[Result]): Future[Result] = {
    SessionRecruteurPEConnect
      .get(request.session)
      .map(idTokenPEConnect => block(RecruteurPEConnectRequest(idTokenPEConnect, request)))
      .getOrElse(Future.successful(Results.Status(UNAUTHORIZED)))
  }
}
