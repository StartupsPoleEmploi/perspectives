package authentification.infra.autologin

import authentification.{CandidatAuthentifieRequest, SessionCandidatAuthentifie}
import fr.poleemploi.perspectives.authentification.domain.CandidatAuthentifie
import fr.poleemploi.perspectives.authentification.infra.autologin.{AutologinService, JwtToken, TypeUtilisateur}
import fr.poleemploi.perspectives.candidat.{AutologgerCandidatCommand, CandidatCommandHandler, CandidatId}
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, ExisteCandidatQuery}
import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import controllers.FlashMessages._

object SessionCandidatAutologge {

  private val namespace = "autologin"
  private val autologinTokenAttribute = s"$namespace.token"

  def getAutologinToken(session: Session): Option[JwtToken] =
    session.get(autologinTokenAttribute).map(JwtToken)

  def setAutologinToken(jwtToken: JwtToken,
                        session: Session): Session =
    session + (autologinTokenAttribute -> jwtToken.value)

  def remove(session: Session): Session =
    session - autologinTokenAttribute
}

trait AutologinCandidat {

  implicit val executionContext: ExecutionContext

  private val TOKEN_QUERY_PARAM_NAME = "token"

  def autologinService: AutologinService

  def candidatQueryHandler: CandidatQueryHandler

  def candidatCommandHandler: CandidatCommandHandler

  def autologgerCandidat[A](request: Request[A], block: CandidatAuthentifieRequest[A] => Future[Result]): Future[Option[Result]] =
    extraireCandidatAutologge(request).flatMap(_.map(candidatAutologge =>
      block(CandidatAuthentifieRequest(candidatAutologge.candidatAuthentifie, request))
        .map { r =>
          val session = SessionCandidatAutologge.setAutologinToken(candidatAutologge.autologinToken, SessionCandidatAuthentifie.set(candidatAutologge.candidatAuthentifie, r.session(request)))
          Some(r.withSession(session).flashing(request.flash.withCandidatAutologue))
        }
    ).getOrElse(Future(None)))

  private def connecter(candidatId: CandidatId): Future[Unit] =
    candidatCommandHandler.handle(AutologgerCandidatCommand(candidatId))

  private[autologin] def extraireCandidatAutologge[A](request: Request[A]): Future[Option[CandidatAutologge]] =
    request.getQueryString(TOKEN_QUERY_PARAM_NAME).map(token =>
      (for {
        autologinToken <- Future.fromTry(autologinService.extractAutologinToken(token))
        candidatId = CandidatId(autologinToken.identifiant)
        result <- candidatQueryHandler.handle(ExisteCandidatQuery(candidatId)) if autologinToken.typeUtilisateur == TypeUtilisateur.CANDIDAT
        _ <- connecter(candidatId) if result.existe
      } yield Some(CandidatAutologge(
        candidatAuthentifie = CandidatAuthentifie(
          candidatId = candidatId,
          nom = autologinToken.nom,
          prenom = autologinToken.prenom,
          email = autologinToken.email
        ),
        autologinToken = JwtToken(token)
      ))).recover { case t: Throwable =>
        autologinLogger.warn(s"Erreur lors de l'autologin du candidat avec le token $token", t)
        None
      }
    ).getOrElse(Future(None))

}

case class OptionalCandidatAutologgeRequest[A](autologinToken: Option[JwtToken],
                                               request: Request[A]) extends WrappedRequest[A](request) {

  def isCandidatAutologge: Boolean = autologinToken.isDefined
}

class OptionalCandidatAutologgeAction @Inject()(override val parser: BodyParsers.Default)
                                               (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[OptionalCandidatAutologgeRequest, AnyContent] {

  override def invokeBlock[A](request: Request[A], block: OptionalCandidatAutologgeRequest[A] => Future[Result]): Future[Result] =
    block(OptionalCandidatAutologgeRequest(SessionCandidatAutologge.getAutologinToken(request.session), request))
}

case class CandidatAutologgeRequest[A](autologinToken: JwtToken,
                                       request: Request[A]) extends WrappedRequest[A](request)

class CandidatAutologgeAction @Inject()(override val parser: BodyParsers.Default)
                                       (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[CandidatAutologgeRequest, AnyContent] with Results {

  override def invokeBlock[A](request: Request[A], block: CandidatAutologgeRequest[A] => Future[Result]): Future[Result] =
    SessionCandidatAutologge
      .getAutologinToken(request.session)
      .map(autologinToken => block(CandidatAutologgeRequest(autologinToken, request)))
      .getOrElse(Future.successful(Unauthorized))
}

case class CandidatAutologge(candidatAuthentifie: CandidatAuthentifie, autologinToken: JwtToken)
