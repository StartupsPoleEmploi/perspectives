package authentification

import controllers.FlashMessages._
import controllers.recruteur.routes
import fr.poleemploi.perspectives.authentification.domain.RecruteurAuthentifie
import fr.poleemploi.perspectives.projections.recruteur.{ProfilRecruteurQuery, RecruteurQueryHandler}
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
      .getOrElse(Future.successful(Unauthorized))
}

class RecruteurAConnecterSiNonAuthentifieAction @Inject()(override val parser: BodyParsers.Default)
                                                         (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[RecruteurAuthentifieRequest, AnyContent] with Results {

  override def invokeBlock[A](request: Request[A], block: RecruteurAuthentifieRequest[A] => Future[Result]): Future[Result] =
    SessionRecruteurAuthentifie
      .get(request.session)
      .map(recruteur =>
        block(RecruteurAuthentifieRequest(recruteur, request))
          .map(r => r.withSession(SessionUtilisateurNonAuthentifie.remove(r.session(request))))
      ).getOrElse(
      Future(Redirect(routes.AuthentificationController.connexion())
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

/**
  * N'invoque l'action que si le recruteur n'est pas déjà authentifié, sinon il est redirigé. <br />
  * Cela évite de pouvoir rejouer des actions de connexion qui ne le sont pas (tokens déjà validés, etc.) et qui pourraient redéclencher des authentifications et des événements
  */
class RecruteurNonAuthentifieAction @Inject()(override val parser: BodyParsers.Default,
                                              recruteurQueryHandler: RecruteurQueryHandler)
                                             (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[Request, AnyContent] with Results {

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] =
    SessionRecruteurAuthentifie
      .get(request.session)
      .map(recruteur =>
        recruteurQueryHandler.handle(ProfilRecruteurQuery(recruteur.recruteurId)).map(profil =>
          if (profil.profilComplet)
            Redirect(routes.RechercheCandidatController.rechercherCandidats())
          else
            Redirect(routes.ProfilController.modificationProfil())
              .flashing(request.flash.withMessageAlerte("Veuillez finaliser la saisie de votre profil"))
        )
      ).getOrElse(block(request))
}