package controllers.recruteur

import authentification.infra.peconnect.{RecruteurAuthentifiePEConnectAction, RecruteurAuthentifiePEConnectRequest, SessionOauthTokens, SessionRecruteurPEConnect}
import authentification.infra.play._
import conf.WebAppConfig
import controllers.FlashMessages._
import fr.poleemploi.perspectives.authentification.domain.RecruteurAuthentifie
import fr.poleemploi.perspectives.authentification.infra.peconnect.PEConnectAuthAdapter
import fr.poleemploi.perspectives.commun.EitherUtils._
import fr.poleemploi.perspectives.commun.infra.oauth.OauthConfig
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
class PEConnectController @Inject()(cc: ControllerComponents,
                                    webAppConfig: WebAppConfig,
                                    recruteurNonAuthentifieAction: RecruteurNonAuthentifieAction,
                                    recruteurAuthentifieAction: RecruteurAuthentifieAction,
                                    recruteurAuthentifiePEConnectAction: RecruteurAuthentifiePEConnectAction,
                                    recruteurCommandHandler: RecruteurCommandHandler,
                                    recruteurQueryHandler: RecruteurQueryHandler,
                                    peConnectAuthAdapter: PEConnectAuthAdapter,
                                    peConnectAdapter: PEConnectAdapter) extends AbstractController(cc) with Logging {

  val redirectUri: Call = routes.PEConnectController.connexionCallback()
  val oauthConfig: OauthConfig = webAppConfig.recruteurOauthConfig

  def inscription: Action[AnyContent] = recruteurNonAuthentifieAction.async { implicit request =>
    Future(Redirect(routes.PEConnectController.connexion()).withSession(
      SessionOauthTokens.setOauthTokensRecruteur(peConnectAuthAdapter.generateTokens, request.session)
    ))
  }

  def connexion: Action[AnyContent] = recruteurNonAuthentifieAction.async { implicit request =>
    SessionOauthTokens.getOauthTokensRecruteur(request.session)
      .map(oauthTokens =>
        Future(Redirect(
          url = s"${oauthConfig.urlAuthentification}/connexion/oauth2/authorize",
          status = SEE_OTHER,
          queryString = Map(
            "realm" -> Seq(s"/${oauthConfig.realm}"),
            "response_type" -> Seq("code"),
            "client_id" -> Seq(oauthConfig.clientId),
            "scope" -> Seq(OauthConfig.scopes(oauthConfig)),
            "redirect_uri" -> Seq(redirectUri.absoluteURL()),
            "state" -> Seq(oauthTokens.state),
            "nonce" -> Seq(oauthTokens.nonce)
          )
        )).recover {
          case t: Throwable =>
            logger.error("Erreur lors de la connexion recruteur PEConnect", t)
            // Nettoyage de session et redirect
            Redirect(routes.LandingController.landing()).withSession(
              SessionOauthTokens.removeOauthTokensRecruteur(request.session)
            )
        })
      .getOrElse(
        Future(
          Redirect(routes.LandingController.landing())
            .flashing(request.flash.withMessageErreur("Une erreur est survenue lors de la connexion, veuillez réessayer ultérieurement"))
        )
      )
  }

  def connexionCallback: Action[AnyContent] = recruteurNonAuthentifieAction.async { implicit request =>
    (for {
      authorizationCode <- request.getQueryString("code").toRight("Aucun code d'autorisation n'a été retourné").toFuture
      stateCallback <- request.getQueryString("state").toRight("Aucun state n'a été retourné").toFuture
      oauthTokens <- SessionOauthTokens.getOauthTokensRecruteur(request.session).toRight("Aucun token n'a été stocké en session").toFuture
      accessTokenResponse <- peConnectAuthAdapter.accessToken(
        authorizationCode = authorizationCode,
        redirectUri = redirectUri.absoluteURL(),
        state = stateCallback,
        oauthTokens = oauthTokens,
        oauthConfig = oauthConfig
      )
      recruteurInfos <- peConnectAdapter.infosRecruteur(accessTokenResponse.accessToken)
      optRecruteurPEConnnect <- peConnectAdapter.findRecruteur(recruteurInfos.peConnectId)
      optProfilRecruteur <- optRecruteurPEConnnect.map(r => recruteurQueryHandler.handle(ProfilRecruteurQuery(r.recruteurId)).map(Some(_))).getOrElse(Future.successful(None))
      recruteurId <- optRecruteurPEConnnect.map(r => connecter(r, recruteurInfos)).getOrElse(inscrire(recruteurInfos))
    } yield {
      val recruteurAuthentifie = RecruteurAuthentifie(
        recruteurId = recruteurId,
        nom = recruteurInfos.nom,
        prenom = recruteurInfos.prenom
      )
      val session = SessionRecruteurPEConnect.setJWTToken(accessTokenResponse.idToken, SessionRecruteurAuthentifie.set(recruteurAuthentifie, SessionOauthTokens.removeOauthTokensRecruteur(request.session)))

      if (optProfilRecruteur.exists(_.profilComplet)) {
        SessionUtilisateurNonAuthentifie.getUriConnexion(request.session)
          .map(uri => Redirect(uri).withSession(SessionUtilisateurNonAuthentifie.remove(session)))
          .getOrElse(Redirect(routes.RechercheCandidatController.rechercherCandidats()).withSession(session))
      }
      else if (optProfilRecruteur.isDefined)
        Redirect(routes.ProfilController.modificationProfil()).withSession(session)
          .flashing(request.flash.withMessageAlerte("Veuillez finaliser la saisie de votre profil"))
      else
        Redirect(routes.ProfilController.modificationProfil()).withSession(session).flashing(request.flash.withRecruteurInscrit)
    }).recover {
      case t: Throwable =>
        logger.error(s"Erreur lors du callback recruteur PEConnect avec la requete ${request.rawQueryString}", t)
        // Nettoyage de session et redirect
        Redirect(routes.LandingController.landing()).withSession(SessionOauthTokens.removeOauthTokensRecruteur(request.session))
          .flashing(request.flash.withMessageErreur("Notre service en actuellement en cours de maintenance, veuillez réessayer ultérieurement."))
    }
  }

  def deconnexion: Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    recruteurAuthentifiePEConnectAction.async { implicit recruteurAuthentifiePEConnectRequest: RecruteurAuthentifiePEConnectRequest[AnyContent] =>
      Future(Redirect(
        url = s"${oauthConfig.urlAuthentification}/compte/deconnexion",
        status = SEE_OTHER,
        queryString = Map(
          "id_token_hint" -> Seq(recruteurAuthentifiePEConnectRequest.idTokenPEConnect.value),
          "redirect_uri" -> Seq(routes.PEConnectController.deconnexionCallback().absoluteURL())
        )
      )).recover {
        case t: Throwable =>
          logger.error("Erreur lors de la déconnexion recruteur PEConnect", t)
          // Nettoyage de session et redirect
          Redirect(routes.LandingController.landing()).withSession(
            SessionRecruteurAuthentifie.remove(SessionRecruteurPEConnect.remove(recruteurAuthentifiePEConnectRequest.session))
          )
      }
    }(recruteurAuthentifieRequest)
  }

  def deconnexionCallback: Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    recruteurAuthentifiePEConnectAction.async { implicit recruteurAuthentifiePEConnectRequest: RecruteurAuthentifiePEConnectRequest[AnyContent] =>
      // Nettoyage de session et redirect
      Future(Redirect(routes.LandingController.landing()).withSession(
        SessionRecruteurAuthentifie.remove(SessionRecruteurPEConnect.remove(recruteurAuthentifiePEConnectRequest.session))
      ))
    }(recruteurAuthentifieRequest)
  }

  private def inscrire(peConnectRecruteurInfos: PEConnectRecruteurInfos): Future[RecruteurId] = {
    val recruteurId = recruteurCommandHandler.newId
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
    } yield recruteurId
  }

  private def connecter(recruteurPEConnect: RecruteurPEConnect,
                        peConnectRecruteurInfos: PEConnectRecruteurInfos): Future[RecruteurId] = {
    val recruteurId = recruteurPEConnect.recruteurId
    val command = ConnecterRecruteurCommand(
      id = recruteurId,
      nom = peConnectRecruteurInfos.nom,
      prenom = peConnectRecruteurInfos.prenom,
      email = peConnectRecruteurInfos.email,
      genre = peConnectRecruteurInfos.genre
    )
    recruteurCommandHandler.handle(command).map(_ => recruteurId)
  }
}
