package controllers.candidat

import authentification.infra.play._
import conf.WebAppConfig
import controllers.FlashMessages._
import fr.poleemploi.perspectives.authentification.domain.CandidatAuthentifie
import fr.poleemploi.perspectives.authentification.infra.peconnect.PEConnectAuthAdapter
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.commun.EitherUtils._
import fr.poleemploi.perspectives.commun.infra.oauth.OauthConfig
import fr.poleemploi.perspectives.commun.infra.peconnect.{CandidatPEConnect, PEConnectAdapter}
import fr.poleemploi.perspectives.commun.infra.peconnect.ws.{AccessToken, PEConnectCandidatInfos}
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, CandidatSaisieCriteresRechercheQuery}
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PEConnectController @Inject()(cc: ControllerComponents,
                                    webAppConfig: WebAppConfig,
                                    candidatCommandHandler: CandidatCommandHandler,
                                    candidatQueryHandler: CandidatQueryHandler,
                                    candidatPEConnectAction: CandidatPEConnectAction,
                                    peConnectAuthAdapter: PEConnectAuthAdapter,
                                    peConnectAdapter: PEConnectAdapter) extends AbstractController(cc) {

  val redirectUri: Call = routes.PEConnectController.connexionCallback()
  val oauthConfig: OauthConfig = webAppConfig.candidatOauthConfig

  def inscription: Action[AnyContent] = Action { request =>
    Redirect(routes.PEConnectController.connexion()).withSession(
      SessionOauthTokens.setOauthTokensCandidat(peConnectAuthAdapter.generateTokens, request.session)
    )
  }

  def connexion: Action[AnyContent] = Action.async { implicit request =>
    SessionOauthTokens.getOauthTokensCandidat(request.session).toRight("Aucun token n'a été stocké en session").toFuture
      .map(oauthTokens =>
        Redirect(
          url = s"${oauthConfig.urlAuthentification}/connexion/oauth2/authorize",
          status = SEE_OTHER,
          queryString = Map(
            "realm" -> Seq(s"/${oauthConfig.realm}"),
            "response_type" -> Seq("code"),
            "client_id" -> Seq(oauthConfig.clientId),
            "scope" -> Seq(s"application_${oauthConfig.clientId} api_peconnect-individuv1 api_peconnect-coordonneesv1 api_peconnect-statutv1 openid profile email coordonnees statut"),
            "redirect_uri" -> Seq(redirectUri.absoluteURL()),
            "state" -> Seq(oauthTokens.state),
            "nonce" -> Seq(oauthTokens.nonce)
          )
        )).recover {
      case t: Throwable =>
        Logger.error("Erreur lors de la connexion candidat via PEConnect", t)
        // Nettoyage de session et redirect
        Redirect(routes.LandingController.landing())
          .withSession(SessionOauthTokens.removeOauthTokensCandidat(request.session))
          .flashing(request.flash.withMessageErreur("Une erreur est survenue lors de l'accès au service de Pôle Emploi, veuillez réessayer ultérieurement"))
    }
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
      infosCandidat <- peConnectAdapter.getInfosCandidat(accessTokenResponse.accessToken)
      // FIXME : adresse et statutDemandeurEmploi à récupérer en async via un PEConnectProcessManager + simplifier commandes inscrire et connecter
      optAdresse <- findAdresseCandidat(accessTokenResponse.accessToken)
      optStatutDemandeurEmploi <- findStatutDemandeurEmploi(accessTokenResponse.accessToken)
      optCandidat <- peConnectAdapter.findCandidat(infosCandidat.peConnectId)
      optCriteresRecherche <- optCandidat.map(c => candidatQueryHandler.handle(CandidatSaisieCriteresRechercheQuery(c.candidatId)).map(Some(_))).getOrElse(Future.successful(None))
      candidatId <- optCandidat.map(c => connecter(c, infosCandidat, optAdresse, optStatutDemandeurEmploi))
        .getOrElse(inscrire(
          peConnectCandidatInfos = infosCandidat,
          adresse = optAdresse,
          statutDemandeurEmploi = optStatutDemandeurEmploi
        ))
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
      case t: Throwable =>
        Logger.error("Erreur lors du callback candidat PEConnect", t)
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
        Logger.error("Erreur lors de la déconnexion candidat via PEConnect", t)
        // Nettoyage de session et redirect
        Redirect(routes.LandingController.landing()).withSession(
          SessionCandidatAuthentifie.remove(SessionCandidatPEConnect.remove(request.session))
        )
    }
  }

  def deconnexionCallback: Action[AnyContent] = candidatPEConnectAction.async { implicit request: CandidatPEConnectRequest[AnyContent] =>
    // Nettoyage de session et redirect
    Future(Redirect(routes.LandingController.landing()).withSession(
      SessionCandidatAuthentifie.remove(SessionCandidatPEConnect.remove(request.session))
    ))
  }

  private def inscrire(peConnectCandidatInfos: PEConnectCandidatInfos,
                       adresse: Option[Adresse],
                       statutDemandeurEmploi: Option[StatutDemandeurEmploi]): Future[CandidatId] = {
    val candidatId = candidatCommandHandler.newId
    val command = InscrireCandidatCommand(
      id = candidatId,
      nom = peConnectCandidatInfos.nom,
      prenom = peConnectCandidatInfos.prenom,
      genre = peConnectCandidatInfos.genre,
      email = peConnectCandidatInfos.email,
      adresse = adresse,
      statutDemandeurEmploi = statutDemandeurEmploi
    )
    for {
      _ <- peConnectAdapter.saveCandidat(CandidatPEConnect(
        candidatId = candidatId,
        peConnectId = peConnectCandidatInfos.peConnectId
      ))
      _ <- candidatCommandHandler.handle(command)
    } yield candidatId
  }

  private def connecter(candidatPEConnect: CandidatPEConnect,
                        peConnectCandidatInfos: PEConnectCandidatInfos,
                        adresse: Option[Adresse],
                        statutDemandeurEmploi: Option[StatutDemandeurEmploi]): Future[CandidatId] = {
    val candidatId = candidatPEConnect.candidatId
    val command = ConnecterCandidatCommand(
      id = candidatId,
      nom = peConnectCandidatInfos.nom,
      prenom = peConnectCandidatInfos.prenom,
      genre = peConnectCandidatInfos.genre,
      email = peConnectCandidatInfos.email,
      adresse = adresse,
      statutDemandeurEmploi = statutDemandeurEmploi
    )
    candidatCommandHandler.handle(command).map(_ => candidatId)
  }

  private def findAdresseCandidat(accessToken: AccessToken): Future[Option[Adresse]] =
    peConnectAdapter.getAdresseCandidat(accessToken).map(Some(_))
      .recoverWith {
        case t: Throwable =>
          Logger.error("Erreur lors de la récupération de l'adresse", t)
          Future.successful(None)
      }

  private def findStatutDemandeurEmploi(accessToken: AccessToken): Future[Option[StatutDemandeurEmploi]] =
    peConnectAdapter.getStatutDemandeurEmploiCandidat(accessToken).map(Some(_))
      .recoverWith {
        case t: Throwable =>
          Logger.error("Erreur lors de la récupération du statut demandeur d'emploi", t)
          Future.successful(None)
      }
}
