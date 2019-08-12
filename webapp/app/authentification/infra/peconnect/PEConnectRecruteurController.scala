package authentification.infra.peconnect

import authentification._
import conf.WebAppConfig
import controllers.FlashMessages._
import fr.poleemploi.perspectives.authentification.domain.RecruteurAuthentifie
import fr.poleemploi.perspectives.authentification.infra.peconnect.PEConnectAuthAdapter
import fr.poleemploi.perspectives.commun.infra.peconnect.ws.PEConnectRecruteurInfos
import fr.poleemploi.perspectives.commun.infra.peconnect.{PEConnectAdapter, RecruteurPEConnect}
import fr.poleemploi.perspectives.projections.recruteur.{ProfilRecruteurQuery, RecruteurQueryHandler}
import fr.poleemploi.perspectives.recruteur.{ConnecterRecruteurCommand, InscrireRecruteurCommand, RecruteurCommandHandler, RecruteurId}
import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PEConnectRecruteurController @Inject()(cc: ControllerComponents,
                                             webAppConfig: WebAppConfig,
                                             recruteurNonAuthentifieAction: RecruteurNonAuthentifieAction,
                                             recruteurAuthentifieAction: RecruteurAuthentifieAction,
                                             recruteurAuthentifiePEConnectAction: RecruteurAuthentifiePEConnectAction,
                                             recruteurCommandHandler: RecruteurCommandHandler,
                                             recruteurQueryHandler: RecruteurQueryHandler,
                                             peConnectAuthAdapter: PEConnectAuthAdapter,
                                             peConnectAdapter: PEConnectAdapter) extends AbstractController(cc) with Logging {

  val authentificationPEConnect = new AuthPEConnect(
    peConnectAuthAdapter = peConnectAuthAdapter,
    oauthConfig = webAppConfig.recruteurOauthConfig,
    connexionCallbackURL = authentification.infra.peconnect.routes.PEConnectRecruteurController.connexionCallback(),
    deconnexionCallbackURL = authentification.infra.peconnect.routes.PEConnectRecruteurController.deconnexionCallback()
  )

  def connexion: Action[AnyContent] = recruteurNonAuthentifieAction { implicit request =>
    SessionOauthTokensRecruteur.getOauthTokensRecruteur(request.session)
      .map(oauthTokens =>
        authentificationPEConnect.connexion(oauthTokens)
      ).getOrElse(
      Redirect(authentification.infra.peconnect.routes.PEConnectRecruteurController.connexion())
        .withSession(SessionOauthTokensRecruteur.setOauthTokensRecruteur(peConnectAuthAdapter.generateTokens, request.session))
        .withHeaders(("X-Robots-Tag", "none"))
    )
  }

  def connexionCallback: Action[AnyContent] = recruteurNonAuthentifieAction.async { implicit request =>
    authentificationPEConnect.connexionCallback(
      oauthTokens = SessionOauthTokensRecruteur.getOauthTokensRecruteur(request.session).getOrElse(throw new IllegalArgumentException("Aucun token n'a été stocké en session")),
      () => Future(
        Redirect(controllers.recruteur.routes.LandingController.landing())
          .withSession(SessionOauthTokensRecruteur.removeOauthTokensRecruteur(request.session))
      ),
      accessTokenResponse =>
        for {
          recruteurInfos <- peConnectAdapter.infosRecruteur(accessTokenResponse.accessToken)
          optRecruteurPEConnnect <- peConnectAdapter.findRecruteur(recruteurInfos.peConnectId)
          recruteurId = optRecruteurPEConnnect.map(_.recruteurId).getOrElse(recruteurCommandHandler.newId)
          _ <- optRecruteurPEConnnect
            .map(r =>
              if (webAppConfig.recruteursPEConnectTesteurs.contains(r.peConnectId))
                Future.successful(())
              else
                connecter(r.recruteurId, recruteurInfos)
            ).getOrElse(inscrire(recruteurId, recruteurInfos))
          optProfilRecruteur <- optRecruteurPEConnnect.map(r => recruteurQueryHandler.handle(ProfilRecruteurQuery(r.recruteurId)).map(Some(_))).getOrElse(Future.successful(None))
        } yield {
          val recruteurAuthentifie = RecruteurAuthentifie(
            recruteurId = recruteurId,
            nom = recruteurInfos.nom,
            prenom = recruteurInfos.prenom
          )
          val session = SessionRecruteurPEConnect.setJWTToken(accessTokenResponse.idToken, SessionRecruteurAuthentifie.set(recruteurAuthentifie, SessionOauthTokensRecruteur.removeOauthTokensRecruteur(request.session)))

          if (optProfilRecruteur.exists(_.profilComplet))
            SessionUtilisateurNonAuthentifie.getUriConnexion(request.session)
              .map(uri => Redirect(uri).withSession(SessionUtilisateurNonAuthentifie.remove(session)))
              .getOrElse(Redirect(controllers.recruteur.routes.RechercheCandidatController.rechercherCandidats()).withSession(session))
          else if (optProfilRecruteur.isDefined)
            Redirect(controllers.recruteur.routes.ProfilController.modificationProfil()).withSession(session)
              .flashing(request.flash.withMessageAlerte("Veuillez finaliser la saisie de votre profil"))
          else
            Redirect(controllers.recruteur.routes.ProfilController.modificationProfil()).withSession(session)
              .flashing(request.flash.withRecruteurInscrit)
        }
    ).recover {
      case t: Throwable =>
        logger.error(s"Erreur lors du callback recruteur PEConnect avec la requete ${request.rawQueryString}", t)
        // Nettoyage de session et redirect
        Redirect(controllers.recruteur.routes.LandingController.landing())
          .withSession(SessionOauthTokensRecruteur.removeOauthTokensRecruteur(request.session))
          .flashing(request.flash.withMessageErreur("Notre service en actuellement en cours de maintenance, veuillez réessayer ultérieurement."))
    }
  }

  def deconnexion: Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    recruteurAuthentifiePEConnectAction { implicit recruteurAuthentifiePEConnectRequest: RecruteurAuthentifiePEConnectRequest[AnyContent] =>
      authentificationPEConnect.deconnexion
    }(recruteurAuthentifieRequest)
  }

  def deconnexionCallback: Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    recruteurAuthentifiePEConnectAction.async { implicit recruteurAuthentifiePEConnectRequest: RecruteurAuthentifiePEConnectRequest[AnyContent] =>
      // Nettoyage de session et redirect
      Future(Redirect(controllers.recruteur.routes.LandingController.landing()).withSession(
        SessionRecruteurAuthentifie.remove(SessionRecruteurPEConnect.remove(recruteurAuthentifiePEConnectRequest.session))
      ))
    }(recruteurAuthentifieRequest)
  }

  private def inscrire(recruteurId: RecruteurId,
                       peConnectRecruteurInfos: PEConnectRecruteurInfos): Future[Unit] = {
    val command = InscrireRecruteurCommand(
      id = recruteurId,
      nom = peConnectRecruteurInfos.nom,
      prenom = peConnectRecruteurInfos.prenom,
      email = peConnectRecruteurInfos.email,
      genre = peConnectRecruteurInfos.genre
    )
    for {
      _ <- peConnectAdapter.saveRecruteur(RecruteurPEConnect(
        recruteurId = recruteurId,
        peConnectId = peConnectRecruteurInfos.peConnectId
      ))
      _ <- recruteurCommandHandler.handle(command)
    } yield ()
  }

  private def connecter(recruteurId: RecruteurId,
                        peConnectRecruteurInfos: PEConnectRecruteurInfos): Future[Unit] =
    recruteurCommandHandler.handle(ConnecterRecruteurCommand(
      id = recruteurId,
      nom = peConnectRecruteurInfos.nom,
      prenom = peConnectRecruteurInfos.prenom,
      email = peConnectRecruteurInfos.email,
      genre = peConnectRecruteurInfos.genre
    ))
}
