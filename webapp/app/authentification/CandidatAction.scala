package authentification

import controllers.FlashMessages._
import controllers.candidat.routes
import fr.poleemploi.perspectives.authentification.domain.CandidatAuthentifie
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, CandidatSaisieCriteresRechercheQuery}
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
      .getOrElse(Future.successful(Unauthorized))
}

class CandidatAConnecterSiNonAuthentifieAction @Inject()(override val parser: BodyParsers.Default)
                                                        (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[CandidatAuthentifieRequest, AnyContent] with Results {

  override def invokeBlock[A](request: Request[A], block: CandidatAuthentifieRequest[A] => Future[Result]): Future[Result] =
    SessionCandidatAuthentifie
      .get(request.session)
      .map(candidat =>
        block(CandidatAuthentifieRequest(candidat, request))
          .map(r => r.withSession(SessionUtilisateurNonAuthentifie.remove(r.session(request))))
      ).getOrElse(
      Future(Redirect(routes.AuthentificationController.connexion())
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

/**
  * N'invoque l'action que si le candidat n'est pas déjà authentifié, sinon il est redirigé. <br />
  * Cela évite de pouvoir rejouer des actions de connexion qui ne le sont pas (tokens déjà validés, etc.) et qui pourraient redéclencher des authentifications et des événements
  */
class CandidatNonAuthentifieAction @Inject()(override val parser: BodyParsers.Default,
                                             candidatQueryHandler: CandidatQueryHandler)
                                            (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[Request, AnyContent] with Results {

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] =
    SessionCandidatAuthentifie
      .get(request.session)
      .map(candidat =>
        candidatQueryHandler.handle(CandidatSaisieCriteresRechercheQuery(candidat.candidatId)).map(criteresRecherche =>
          if (criteresRecherche.saisieComplete)
            Redirect(routes.RechercheOffreController.index())
          else
            Redirect(routes.SaisieCriteresRechercheController.saisieCriteresRecherche())
              .flashing(request.flash.withMessageAlerte("Veuillez finaliser la saisie de vos critères"))
        )
      ).getOrElse(block(request))
}