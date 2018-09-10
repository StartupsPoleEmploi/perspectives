package controllers.candidat

import authentification.infra.play._
import conf.WebAppConfig
import controllers.FlashMessages._
import fr.poleemploi.perspectives.authentification.domain.CandidatAuthentifie
import fr.poleemploi.perspectives.authentification.infra.PEConnectService
import fr.poleemploi.perspectives.authentification.infra.sql.CandidatPEConnect
import fr.poleemploi.perspectives.authentification.infra.ws.{PEConnectCandidatInfos, PEConnectException, PEConnectWSAdapterConfig}
import fr.poleemploi.perspectives.candidat._
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.mvc._
import utils.EitherUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PEConnectController @Inject()(cc: ControllerComponents,
                                    webAppConfig: WebAppConfig,
                                    candidatCommandHandler: CandidatCommandHandler,
                                    candidatPEConnectAction: CandidatPEConnectAction,
                                    peConnectService: PEConnectService) extends AbstractController(cc) {

  val oauthTokenSessionStorage = new OauthTokenSessionStorage("candidat")
  val redirectUri: Call = routes.PEConnectController.connexionCallback()
  val peConnectConfig: PEConnectWSAdapterConfig = webAppConfig.peConnectCandidatConfig

  def inscription(): Action[AnyContent] = Action { request =>
    Redirect(routes.PEConnectController.connexion()).withSession(
      oauthTokenSessionStorage.set(peConnectService.generateTokens(), request.session)
    )
  }

  def connexion(): Action[AnyContent] = Action.async { implicit request =>
    oauthTokenSessionStorage.get(request.session).toRight("Aucun token n'a été stocké en session").toFuture
      .map(oauthTokens =>
        Redirect(
          url = s"${peConnectConfig.urlAuthentification}/connexion/oauth2/authorize",
          status = SEE_OTHER,
          queryString = Map(
            "realm" -> Seq("/individu"),
            "response_type" -> Seq("code"),
            "client_id" -> Seq(peConnectConfig.clientId),
            "scope" -> Seq(s"application_${peConnectConfig.clientId} api_peconnect-individuv1 api_peconnect-coordonneesv1 api_peconnect-statutv1 openid profile email coordonnees statut"),
            "redirect_uri" -> Seq(redirectUri.absoluteURL()),
            "state" -> Seq(oauthTokens.state),
            "nonce" -> Seq(oauthTokens.nonce)
          )
        )).recover {
      case t: Throwable =>
        Logger.error("Erreur lors de l'authentification PEConnect", t)
        // Nettoyage de session et redirect
        Redirect(routes.LandingController.landing())
          .withSession(oauthTokenSessionStorage.remove(request.session))
          .flashing(request.flash.withMessageErreur("Une erreur est survenue lors de l'accès au service de Pôle Emploi, veuillez réessayer ultérieurement"))
    }
  }

  def connexionCallback(): Action[AnyContent] = Action.async { implicit request =>
    (for {
      authorizationCode <- request.getQueryString("code").toRight("Aucun code d'autorisation n'a été retourné").toFuture
      stateCallback <- request.getQueryString("state").toRight("Aucun state n'a été retourné").toFuture
      oauthTokens <- oauthTokenSessionStorage.get(request.session).toRight("Aucun token n'a été stocké en session").toFuture
      accessTokenResponse <- peConnectService.getAccessTokenCandidat(
        authorizationCode = authorizationCode,
        redirectUri = redirectUri.absoluteURL()
      )
      _ <- Either.cond(peConnectService.verifyState(oauthTokens, stateCallback), (), "La comparaison du state a échoué").toFuture
      _ <- Either.cond(peConnectService.verifyNonce(oauthTokens, accessTokenResponse.nonce), (), "La comparaison du nonce a échoué").toFuture
      _ <- peConnectService.validateAccessToken(accessTokenResponse)
      infosCandidat <- peConnectService.getInfosCandidat(accessTokenResponse.accessToken)
      adresse <- peConnectService.getAdresseCandidat(accessTokenResponse.accessToken)
      statutDemandeurEmploi <- peConnectService.getStatutDemandeurEmploiCandidat(accessTokenResponse.accessToken)
      // FIXME : saga pour l'adresse et le statut (l'inscription est prioritaire)
      optCandidat <- peConnectService.findCandidat(infosCandidat.peConnectId)
      candidatId <- optCandidat.map(c => modifierProfil(c, infosCandidat, adresse, statutDemandeurEmploi))
        .getOrElse(inscrire(
          peConnectCandidatInfos = infosCandidat,
          adresse = adresse,
          statutDemandeurEmploi = statutDemandeurEmploi
        ))
    } yield {
      val candidatAuthentifie = CandidatAuthentifie(
        candidatId = candidatId,
        nom = infosCandidat.nom,
        prenom = infosCandidat.prenom
      )
      val session = SessionCandidatPEConnect.set(accessTokenResponse.idToken, SessionCandidatAuthentifie.set(candidatAuthentifie, oauthTokenSessionStorage.remove(request.session)))

      if (optCandidat.isDefined)
        Redirect(routes.SaisieCriteresRechercheController.saisieCriteresRecherche()).withSession(session)
      else
        Redirect(routes.SaisieCriteresRechercheController.saisieCriteresRecherche()).withSession(session)
          .flashing(request.flash.withCandidatInscrit)
    }).recover {
      case t: PEConnectException =>
        Logger.error("Erreur lors du callback PEConnect", t)
        // Nettoyage de session et redirect
        Redirect(routes.LandingController.landing())
          .withSession(oauthTokenSessionStorage.remove(request.session))
          .flashing(request.flash.withMessageErreur("Une erreur est survenue lors de l'accès au service de Pôle Emploi, veuillez réessayer ultérieurement"))
      case t: Throwable =>
        Logger.error("Erreur lors du callback PEConnect", t)
        // Nettoyage de session et redirect
        Redirect(routes.LandingController.landing()).withSession(
          oauthTokenSessionStorage.remove(request.session)
        )
    }
  }

  def deconnexion(): Action[AnyContent] = candidatPEConnectAction.async { implicit request: CandidatPEConnectRequest[AnyContent] =>
    peConnectService.deconnexionCandidat(
      idToken = request.idTokenPEConnect,
      redirectUri = routes.LandingController.landing().absoluteURL()
    ).map { _ =>
      // Nettoyage de session et redirect
      Redirect(routes.LandingController.landing()).withSession(
        SessionCandidatAuthentifie.remove(SessionCandidatPEConnect.remove(request.session))
      )
    }.recover {
      case t: Throwable =>
        Logger.error("Erreur lors de la déconnexion PEConnect", t)
        // Nettoyage de session et redirect
        Redirect(routes.LandingController.landing()).withSession(
          SessionCandidatAuthentifie.remove(SessionCandidatPEConnect.remove(request.session))
        )
    }
  }

  private def inscrire(peConnectCandidatInfos: PEConnectCandidatInfos,
                       adresse: Adresse,
                       statutDemandeurEmploi: StatutDemandeurEmploi): Future[CandidatId] = {
    val candidatId = candidatCommandHandler.newCandidatId
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
      _ <- peConnectService.saveCandidat(CandidatPEConnect(
        candidatId = candidatId,
        peConnectId = peConnectCandidatInfos.peConnectId
      ))
      _ <- candidatCommandHandler.inscrire(command)
    } yield candidatId
  }

  private def modifierProfil(candidatPEConnect: CandidatPEConnect,
                             peConnectCandidatInfos: PEConnectCandidatInfos,
                             adresse: Adresse,
                             statutDemandeurEmploi: StatutDemandeurEmploi): Future[CandidatId] = {
    val candidatId = candidatPEConnect.candidatId
    val command = ModifierProfilCommand(
      id = candidatId,
      nom = peConnectCandidatInfos.nom,
      prenom = peConnectCandidatInfos.prenom,
      genre = peConnectCandidatInfos.genre,
      email = peConnectCandidatInfos.email,
      adresse = adresse,
      statutDemandeurEmploi = statutDemandeurEmploi
    )
    candidatCommandHandler.modifierProfil(command).map(_ => candidatId)
  }
}
