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

class ConseillerAdminAuthentifieAction @Inject()(override val parser: BodyParsers.Default,
                                                 sessionConseillerAuthentifie: SessionConseillerAuthentifie,
                                                 autorisationService: AutorisationService)
                                                (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[ConseillerAuthentifieRequest, AnyContent] {

  override def invokeBlock[A](request: Request[A], block: ConseillerAuthentifieRequest[A] => Future[Result]): Future[Result] = {
    sessionConseillerAuthentifie
      .get(request.session)
      .flatMap(c => if (autorisationService.hasRole(c.conseillerId, RoleConseiller.ADMIN)) Some(c) else None)
      .map(c => block(ConseillerAuthentifieRequest(c, request)))
      .getOrElse(Future.successful(Results.Status(UNAUTHORIZED)))
  }
}