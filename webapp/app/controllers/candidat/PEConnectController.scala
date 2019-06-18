package controllers.candidat

import authentification.infra.play._
import conf.WebAppConfig
import controllers.FlashMessages._
import fr.poleemploi.perspectives.authentification.domain.CandidatAuthentifie
import fr.poleemploi.perspectives.authentification.infra.peconnect.PEConnectAuthAdapter
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.commun.EitherUtils._
import fr.poleemploi.perspectives.commun.infra.oauth.OauthConfig
import fr.poleemploi.perspectives.commun.infra.peconnect.ws.PEConnectCandidatInfos
import fr.poleemploi.perspectives.commun.infra.peconnect.{CandidatPEConnect, PEConnectAccessTokenStorage, PEConnectAdapter}
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, CandidatSaisieCriteresRechercheQuery}
import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PEConnectController @Inject()(cc: ControllerComponents,
                                    webAppConfig: WebAppConfig,
                                    candidatCommandHandler: CandidatCommandHandler,
                                    candidatQueryHandler: CandidatQueryHandler,
                                    candidatPEConnectAction: CandidatPEConnectAction,
                                    candidatAuthentifieAction: CandidatAuthentifieAction,
                                    peConnectAuthAdapter: PEConnectAuthAdapter,
                                    peConnectAdapter: PEConnectAdapter,
                                    peConnectAccessTokenStorage: PEConnectAccessTokenStorage) extends AbstractController(cc) with Logging {

  val redirectUri: Call = routes.PEConnectController.connexionCallback()
  val oauthConfig: OauthConfig = webAppConfig.candidatOauthConfig

  def inscription: Action[AnyContent] = Action { request =>
    Redirect(routes.PEConnectController.connexion()).withSession(
      SessionOauthTokens.setOauthTokensCandidat(peConnectAuthAdapter.generateTokens, request.session)
    )
  }

  def connexion: Action[AnyContent] = Action.async { implicit request =>
    SessionOauthTokens.getOauthTokensCandidat(request.session)
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
            logger.error("Erreur lors de la connexion candidat PEConnect", t)
            // Nettoyage de session et redirect
            Redirect(routes.LandingController.landing())
              .withSession(SessionOauthTokens.removeOauthTokensCandidat(request.session))
              .flashing(request.flash.withMessageErreur("Une erreur est survenue lors de l'accès au service de Pôle Emploi, veuillez réessayer ultérieurement"))
        })
      .getOrElse(
        Future(
          Redirect(routes.LandingController.landing())
            .flashing(request.flash.withMessageErreur("Une erreur est survenue lors de la connexion, veuillez réessayer ultérieurement"))
        )
      )
  }

  def connexionCallback: Action[AnyContent] = Action.async { implicit request =>
    (for {
      authorizationCode <- request.getQueryString("code").toRight("Aucun code d'autorisation n'a été retourné").toFuture
      stateCallback <- request.getQueryString("state").toRight("Aucun state n'a été retourné").toFuture
      oauthTokens <- SessionOauthTokens.getOauthTokensCandidat(request.session).toRight("Aucun token n'a été stocké en session").toFuture
      _ <- Either.cond(peConnectAuthAdapter.verifyState(oauthTokens, stateCallback), (), "La comparaison du state a échoué").toFuture
      accessTokenResponse <- peConnectAuthAdapter.getAccessTokenCandidat(
        authorizationCode = authorizationCode,
        redirectUri = redirectUri.absoluteURL(),
        oauthTokens = oauthTokens
      )
      infosCandidat <- peConnectAdapter.infosCandidat(accessTokenResponse.accessToken)
      optCandidatPEConnect <- peConnectAdapter.findCandidat(infosCandidat.peConnectId)
      optCriteresRecherche <- optCandidatPEConnect.map(c => candidatQueryHandler.handle(CandidatSaisieCriteresRechercheQuery(c.candidatId)).map(Some(_))).getOrElse(Future.successful(None))
      candidatId = optCandidatPEConnect.map(_.candidatId).getOrElse(candidatCommandHandler.newId)
      _ <- peConnectAccessTokenStorage.add(candidatId, accessTokenResponse.accessToken)
      _ <- optCandidatPEConnect.map(_ => connecter(candidatId, infosCandidat))
        .getOrElse(inscrire(candidatId, infosCandidat))
    } yield {
      val candidatAuthentifie = CandidatAuthentifie(
        candidatId = candidatId,
        nom = infosCandidat.nom,
        prenom = infosCandidat.prenom
      )
      val session = SessionCandidatPEConnect.setJWTToken(accessTokenResponse.idToken, SessionCandidatAuthentifie.set(candidatAuthentifie, SessionOauthTokens.removeOauthTokensCandidat(request.session)))
      val flash = request.flash.withCandidatConnecte

      if (optCriteresRecherche.exists(_.saisieComplete))
        SessionUtilisateurNonAuthentifie.getUriConnexion(request.session)
          .map(uri => Redirect(uri).withSession(SessionUtilisateurNonAuthentifie.remove(session)).flashing(flash))
          .getOrElse(Redirect(routes.RechercheOffreController.index()).withSession(session).flashing(flash))
      else if (optCriteresRecherche.isDefined)
        Redirect(routes.SaisieCriteresRechercheController.saisieCriteresRecherche()).withSession(session).flashing(flash)
      else
        Redirect(routes.SaisieCriteresRechercheController.saisieCriteresRecherche()).withSession(session)
          .flashing(flash.withCandidatInscrit)
    }).recover {
      case CandidatPEConnectEmailManquantException =>
        Redirect(routes.LandingController.landing())
          .withSession(SessionOauthTokens.removeOauthTokensCandidat(request.session))
          .flashing(request.flash.withMessageErreur("Veuillez rensegner votre adresse email dans votre profil sur https://candidat.pole-emploi.fr et réessayer ensuite"))
      case t: Throwable =>
        logger.error(s"Erreur lors du callback candidat PEConnect avec la requete ${request.rawQueryString}", t)
        // Nettoyage de session et redirect
        Redirect(routes.LandingController.landing())
          .withSession(SessionOauthTokens.removeOauthTokensCandidat(request.session))
          .flashing(request.flash.withMessageErreur("Notre service en actuellement en cours de maintenance, veuillez réessayer ultérieurement."))
    }
  }

  def deconnexion: Action[AnyContent] = candidatPEConnectAction.async { implicit request: CandidatPEConnectRequest[AnyContent] =>
    Future(Redirect(
      url = s"${oauthConfig.urlAuthentification}/compte/deconnexion",
      status = SEE_OTHER,
      queryString = Map(
        "id_token_hint" -> Seq(request.idTokenPEConnect.value),
        "redirect_uri" -> Seq(routes.PEConnectController.deconnexionCallback().absoluteURL())
      )
    )).recover {
      case t: Throwable =>
        logger.error("Erreur lors de la déconnexion candidat PEConnect", t)
        // Nettoyage de session et redirect
        Redirect(routes.LandingController.landing()).withSession(
          SessionCandidatAuthentifie.remove(SessionCandidatPEConnect.remove(request.session))
        )
    }
  }

  def deconnexionCallback: Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    candidatPEConnectAction.async { implicit candidatPEConnectRequest: CandidatPEConnectRequest[AnyContent] =>
      peConnectAccessTokenStorage.remove(candidatAuthentifieRequest.candidatId).map(_ =>
        // Nettoyage de session et redirect
        Redirect(routes.LandingController.landing()).withSession(
          SessionCandidatAuthentifie.remove(SessionCandidatPEConnect.remove(candidatPEConnectRequest.session))
        )
      )
    }(candidatAuthentifieRequest)
  }

  private def inscrire(candidatId: CandidatId,
                       peConnectCandidatInfos: PEConnectCandidatInfos): Future[Unit] = {
    val command = InscrireCandidatCommand(
      id = candidatId,
      nom = peConnectCandidatInfos.nom,
      prenom = peConnectCandidatInfos.prenom,
      genre = peConnectCandidatInfos.genre,
      email = peConnectCandidatInfos.email.getOrElse(throw CandidatPEConnectEmailManquantException)
    )
    for {
      _ <- peConnectAdapter.saveCandidat(CandidatPEConnect(
        candidatId = candidatId,
        peConnectId = peConnectCandidatInfos.peConnectId
      ))
      _ <- candidatCommandHandler.handle(command)
    } yield ()
  }

  private def connecter(candidatId: CandidatId,
                        peConnectCandidatInfos: PEConnectCandidatInfos): Future[Unit] =
    candidatCommandHandler.handle(ConnecterCandidatCommand(
      id = candidatId,
      nom = peConnectCandidatInfos.nom,
      prenom = peConnectCandidatInfos.prenom,
      genre = peConnectCandidatInfos.genre,
      email = peConnectCandidatInfos.email.getOrElse(throw CandidatPEConnectEmailManquantException)
    ))
}

case object CandidatPEConnectEmailManquantException extends Exception