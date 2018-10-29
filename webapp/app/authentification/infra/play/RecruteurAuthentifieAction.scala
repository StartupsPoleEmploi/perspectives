package authentification.infra.play

import controllers.recruteur.routes
import fr.poleemploi.perspectives.authentification.domain.RecruteurAuthentifie
import fr.poleemploi.perspectives.recruteur.RecruteurId
import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class RecruteurAuthentifieRequest[A](recruteurAuthentifie: RecruteurAuthentifie,
                                          request: Request[A]) extends WrappedRequest[A](request) {

  def recruteurId: RecruteurId = recruteurAuthentifie.recruteurId
}

class RecruteurAuthentifieAction @Inject()(override val parser: BodyParsers.Default)
                                          (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[RecruteurAuthentifieRequest, AnyContent] with Results {

  override def invokeBlock[A](request: Request[A], block: RecruteurAuthentifieRequest[A] => Future[Result]): Future[Result] =
    SessionRecruteurAuthentifie
      .get(request.session)
      .map(recruteur => block(RecruteurAuthentifieRequest(recruteur, request)))
      .getOrElse(
        Future.successful(Redirect(routes.InscriptionController.inscription())
          .withSession(SessionUtilisateurNonAuthentifie.setUriConnexion(request.uri, request.session))
        )
      )
}

case class OptionalRecruteurAuthentifieRequest[A](recruteurAuthentifie: Option[RecruteurAuthentifie],
                                                  request: Request[A]) extends WrappedRequest[A](request) {

  def isRecruteurAuthentifie: Boolean = recruteurAuthentifie.isDefined
}

class OptionalRecruteurAuthentifieAction @Inject()(override val parser: BodyParsers.Default)
                                                  (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[OptionalRecruteurAuthentifieRequest, AnyContent] {

  override def invokeBlock[A](request: Request[A], block: OptionalRecruteurAuthentifieRequest[A] => Future[Result]): Future[Result] =
    block(OptionalRecruteurAuthentifieRequest(SessionRecruteurAuthentifie.get(request.session), request))
}