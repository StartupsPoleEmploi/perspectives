package authentification.infra.play

import controllers.FlashMessages._
import controllers.candidat.routes
import fr.poleemploi.perspectives.authentification.domain.CandidatAuthentifie
import fr.poleemploi.perspectives.candidat.CandidatId
import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class CandidatAuthentifieRequest[A](candidatAuthentifie: CandidatAuthentifie,
                                         request: Request[A]) extends WrappedRequest[A](request) {

  def candidatId: CandidatId = candidatAuthentifie.candidatId
}


class CandidatAuthentifieAction @Inject()(override val parser: BodyParsers.Default)
                                         (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[CandidatAuthentifieRequest, AnyContent] with Results {

  override def invokeBlock[A](request: Request[A], block: CandidatAuthentifieRequest[A] => Future[Result]): Future[Result] =
    SessionCandidatAuthentifie
      .get(request.session)
      .map(candidat => block(CandidatAuthentifieRequest(candidat, request)))
      .getOrElse(
        if (request.flash.candidatConnecte) // Un candidat n'est pas authentifié alors qu'il vient de se connecter : erreur de session
          Future.successful(Unauthorized)
        else
          Future.successful(Redirect(routes.InscriptionController.inscription())
            .withSession(SessionUtilisateurNonAuthentifie.setUriConnexion(request.uri, request.session))
          )
      )
}

case class OptionalCandidatAuthentifieRequest[A](candidatAuthentifie: Option[CandidatAuthentifie],
                                                 request: Request[A]) extends WrappedRequest[A](request) {

  def isCandidatAuthentifie: Boolean = candidatAuthentifie.isDefined
}

class OptionalCandidatAuthentifieAction @Inject()(override val parser: BodyParsers.Default)
                                                 (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[OptionalCandidatAuthentifieRequest, AnyContent] {

  override def invokeBlock[A](request: Request[A], block: OptionalCandidatAuthentifieRequest[A] => Future[Result]): Future[Result] =
    block(OptionalCandidatAuthentifieRequest(SessionCandidatAuthentifie.get(request.session), request))
}