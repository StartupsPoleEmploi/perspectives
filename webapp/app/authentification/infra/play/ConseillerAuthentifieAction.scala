package authentification.infra.play

import fr.poleemploi.perspectives.authentification.domain.ConseillerAuthentifie
import fr.poleemploi.perspectives.conseiller.{AutorisationService, ConseillerId, RoleConseiller}
import javax.inject.Inject
import play.api.http.Status.UNAUTHORIZED
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class ConseillerAuthentifieRequest[A](conseillerAuthentifie: ConseillerAuthentifie,
                                           request: Request[A]) extends WrappedRequest[A](request) {

  def conseillerId: ConseillerId = conseillerAuthentifie.conseillerId
}

class ConseillerAuthentifieAction @Inject()(override val parser: BodyParsers.Default)
                                           (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[ConseillerAuthentifieRequest, AnyContent] {

  override def invokeBlock[A](request: Request[A], block: ConseillerAuthentifieRequest[A] => Future[Result]): Future[Result] = {
    SessionConseillerAuthentifie
      .get(request.session)
      .map(candidat => block(ConseillerAuthentifieRequest(candidat, request)))
      .getOrElse(Future.successful(Results.Status(UNAUTHORIZED)))
  }
}

class ConseillerAdminAuthentifieAction @Inject()(override val parser: BodyParsers.Default,
                                            autorisationService: AutorisationService)
                                           (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[ConseillerAuthentifieRequest, AnyContent] {

  override def invokeBlock[A](request: Request[A], block: ConseillerAuthentifieRequest[A] => Future[Result]): Future[Result] = {
    SessionConseillerAuthentifie
      .get(request.session)
      .flatMap(c => if (autorisationService.hasRole(c.conseillerId, RoleConseiller.ADMIN)) Some(c) else None)
      .map(candidat => block(ConseillerAuthentifieRequest(candidat, request)))
      .getOrElse(Future.successful(Results.Status(UNAUTHORIZED)))
  }
}