package authentification.infra.peconnect

import fr.poleemploi.perspectives.authentification.infra.peconnect.jwt.JWTToken
import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

object SessionCandidatPEConnect {

  private val namespace = "candidat"
  private val idTokenAttribute = s"$namespace.idTokenPEConnect"

  def getJWTToken(session: Session): Option[JWTToken] =
    session.get(idTokenAttribute).map(JWTToken)

  def setJWTToken(idTokenPEConnect: JWTToken,
                  session: Session): Session =
    session + (idTokenAttribute -> idTokenPEConnect.value)

  def remove(session: Session): Session =
    session - idTokenAttribute
}

case class CandidatAuthentifiePEConnectRequest[A](idTokenPEConnect: JWTToken,
                                                  request: Request[A]) extends WrappedRequest[A](request)

class CandidatAuthentifiePEConnectAction @Inject()(override val parser: BodyParsers.Default)
                                                  (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[CandidatAuthentifiePEConnectRequest, AnyContent] with Results {

  override def invokeBlock[A](request: Request[A], block: CandidatAuthentifiePEConnectRequest[A] => Future[Result]): Future[Result] =
    SessionCandidatPEConnect
      .getJWTToken(request.session)
      .map(idTokenPEConnect => block(CandidatAuthentifiePEConnectRequest(idTokenPEConnect, request)))
      .getOrElse(Future.successful(Unauthorized))
}