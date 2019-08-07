package controllers.candidat

import authentification.infra.peconnect.{CandidatAuthentifiePEConnectAction, CandidatAuthentifiePEConnectRequest, SessionCandidatPEConnect, SessionOauthTokens}
import authentification.{SessionUtilisateurNonAuthentifie, _}
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

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PEConnectController @Inject()(cc: ControllerComponents,
                                    webAppConfig: WebAppConfig,
                                    candidatNonAuthentifieAction: CandidatNonAuthentifieAction,
                                    candidatAuthentifieAction: CandidatAuthentifieAction,
                                    candidatAuthentifiePEConnectAction: CandidatAuthentifiePEConnectAction,
                                    candidatCommandHandler: CandidatCommandHandler,
                                    candidatQueryHandler: CandidatQueryHandler,
                                    peConnectAuthAdapter: PEConnectAuthAdapter,
                                    peConnectAdapter: PEConnectAdapter,
                                    peConnectAccessTokenStorage: PEConnectAccessTokenStorage)(implicit exec: ExecutionContext) extends AbstractController(cc) with Logging {

  val redirectUri: Call = routes.PEConnectController.connexionCallback()
  val oauthConfig: OauthConfig = webAppConfig.candidatOauthConfig

  def inscription: Action[AnyContent] = candidatNonAuthentifieAction { request =>
    Redirect(routes.PEConnectController.connexion()).withSession(
      SessionOauthTokens.setOauthTokensCandidat(peConnectAuthAdapter.generateTokens, request.session)
    )
  }

  def connexion: Action[AnyContent] = Action { implicit request =>
    SessionOauthTokens.getOauthTokensCandidat(request.session)
      .map(oauthTokens =>
        Redirect(
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
        ))
      .getOrElse(
        Redirect(routes.LandingController.landing())
          .flashing(request.flash.withMessageErreur("Une erreur est survenue lors de la connexion, veuillez réessayer ultérieurement"))
      )
  }

  def connexionCallback: Action[AnyContent] = candidatNonAuthentifieAction.async { implicit request =>
    (for {
      authorizationCode <- request.getQueryString("code").toRight("Aucun code d'autorisation n'a été retourné").toFuture
      stateCallback <- request.getQueryString("state").toRight("Aucun state n'a été retourné").toFuture
      oauthTokens <- SessionOauthTokens.getOauthTokensCandidat(request.session).toRight("Aucun token n'a été stocké en session").toFuture
      accessTokenResponse <- peConnectAuthAdapter.accessToken(
        authorizationCode = authorizationCode,
        redirectUri = redirectUri.absoluteURL(),
        state = stateCallback,
        oauthTokens = oauthTokens,
        oauthConfig = oauthConfig
      )
      infosCandidat <- peConnectAdapter.infosCandidat(accessTokenResponse.accessToken)
      optCandidatPEConnect <- peConnectAdapter.findCandidat(infosCandidat.peConnectId)
      optCriteresRecherche <- optCandidatPEConnect.map(c => candidatQueryHandler.handle(CandidatSaisieCriteresRechercheQuery(c.candidatId)).map(Some(_))).getOrElse(Future.successful(None))
      candidatId = optCandidatPEConnect.map(_.candidatId).getOrElse(candidatCommandHandler.newId)
      _ <- peConnectAccessTokenStorage.add(candidatId, accessTokenResponse)
      _ <- optCandidatPEConnect.map(_ => connecter(candidatId, infosCandidat))
        .getOrElse(inscrire(candidatId, infosCandidat))
    } yield {
      val candidatAuthentifie = CandidatAuthentifie(
        candidatId = candidatId,
        nom = infosCandidat.nom,
        prenom = infosCandidat.prenom
      )
      val session = SessionCandidatPEConnect.setJWTToken(accessTokenResponse.idToken, SessionCandidatAuthentifie.set(candidatAuthentifie, SessionOauthTokens.removeOauthTokensCandidat(request.session)))

      if (optCriteresRecherche.exists(_.saisieComplete))
        SessionUtilisateurNonAuthentifie.getUriConnexion(request.session)
          .map(uri => Redirect(uri).withSession(SessionUtilisateurNonAuthentifie.remove(session)))
          .getOrElse(Redirect(routes.RechercheOffreController.index()).withSession(session))
      else if (optCriteresRecherche.isDefined)
        Redirect(routes.SaisieCriteresRechercheController.saisieCriteresRecherche()).withSession(session)
          .flashing(request.flash.withMessageAlerte("Veuillez finaliser la saisie de vos critères"))
      else
        Redirect(routes.SaisieCriteresRechercheController.saisieCriteresRecherche()).withSession(session)
          .flashing(request.flash.withCandidatInscrit)
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

  def deconnexion: Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    candidatAuthentifiePEConnectAction { implicit candidatAuthentifiePEConnectRequest: CandidatAuthentifiePEConnectRequest[AnyContent] =>
      Redirect(
        url = s"${oauthConfig.urlAuthentification}/compte/deconnexion",
        status = SEE_OTHER,
        queryString = Map(
          "id_token_hint" -> Seq(candidatAuthentifiePEConnectRequest.idTokenPEConnect.value),
          "redirect_uri" -> Seq(routes.PEConnectController.deconnexionCallback().absoluteURL())
        )
      )
    }(candidatAuthentifieRequest)
  }

  def deconnexionCallback: Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    candidatAuthentifiePEConnectAction.async { implicit candidatAuthentifiePEConnectRequest: CandidatAuthentifiePEConnectRequest[AnyContent] =>
      peConnectAccessTokenStorage.remove(candidatAuthentifieRequest.candidatId).map(_ =>
        // Nettoyage de session et redirect
        Redirect(routes.LandingController.landing()).withSession(
          SessionCandidatAuthentifie.remove(SessionCandidatPEConnect.remove(candidatAuthentifiePEConnectRequest.session))
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