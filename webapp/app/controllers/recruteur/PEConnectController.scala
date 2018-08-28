package controllers.recruteur

import authentification.infra.play._
import conf.WebAppConfig
import controllers.FlashMessages._
import fr.poleemploi.perspectives.authentification.domain.RecruteurAuthentifie
import fr.poleemploi.perspectives.authentification.infra.PEConnectService
import fr.poleemploi.perspectives.authentification.infra.sql.RecruteurPEConnect
import fr.poleemploi.perspectives.authentification.infra.ws.{PEConnectRecruteurInfos, PEConnectWSAdapterConfig}
import fr.poleemploi.perspectives.projections.recruteur.{GetRecruteurQuery, RecruteurQueryHandler}
import fr.poleemploi.perspectives.recruteur.{InscrireRecruteurCommand, ModifierProfilPEConnectCommand, RecruteurCommandHandler, RecruteurId}
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.mvc._
import utils.EitherUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PEConnectController @Inject()(cc: ControllerComponents,
                                    webAppConfig: WebAppConfig,
                                    recruteurPEConnectAction: RecruteurPEConnectAction,
                                    peConnectService: PEConnectService,
                                    recruteurCommandHandler: RecruteurCommandHandler,
                                    recruteurQueryHandler: RecruteurQueryHandler) extends AbstractController(cc) {

  val oauthTokenSessionStorage = new OauthTokenSessionStorage("recruteur")
  val redirectUri: Call = routes.PEConnectController.connexionCallback()
  val peConnectConfig: PEConnectWSAdapterConfig = webAppConfig.peConnectRecruteurConfig

  def inscription(): Action[AnyContent] = Action { request =>
    Redirect(routes.PEConnectController.connexion()).withSession(
      oauthTokenSessionStorage.set(peConnectService.generateTokens(), request.session)
    )
  }

  def connexion(): Action[AnyContent] = Action.async { implicit request =>
    oauthTokenSessionStorage.get(request.session).toRight("Aucun token n'a été stocké en session").toFuture
      .map(oauthTokens => Redirect(
        url = s"${peConnectConfig.urlAuthentification}/connexion/oauth2/authorize",
        status = SEE_OTHER,
        queryString = Map(
          "realm" -> Seq("/employeur"),
          "response_type" -> Seq("code"),
          "client_id" -> Seq(peConnectConfig.clientId),
          "scope" -> Seq(s"application_${peConnectConfig.clientId} api_peconnect-entreprisev1 openid profile email"),
          "redirect_uri" -> Seq(redirectUri.absoluteURL()),
          "state" -> Seq(oauthTokens.state),
          "nonce" -> Seq(oauthTokens.nonce)
        )
      )).recover {
      case t: Throwable =>
        Logger.error("Erreur lors de la connexion PEConnect", t)
        // Nettoyage de session et redirect
        Redirect(routes.LandingController.landing()).withSession(
          oauthTokenSessionStorage.remove(request.session)
        )
    }
  }

  def connexionCallback(): Action[AnyContent] = Action.async { implicit request =>
    (for {
      authorizationCode <- request.getQueryString("code").toRight("Aucun code d'autorisation n'a été retourné").toFuture
      stateCallback <- request.getQueryString("state").toRight("Aucun state n'a été retourné").toFuture
      oauthTokens <- oauthTokenSessionStorage.get(request.session).toRight("Aucun token n'a été stocké en session").toFuture
      accessTokenResponse <- peConnectService.getAccessTokenRecruteur(
        authorizationCode = authorizationCode,
        redirectUri = redirectUri.absoluteURL()
      )
      _ <- Either.cond(peConnectService.verifyState(oauthTokens, stateCallback), (), "La comparaison du state a échoué").toFuture
      _ <- Either.cond(peConnectService.verifyNonce(oauthTokens, accessTokenResponse.nonce), (), "La comparaison du nonce a échoué").toFuture
      _ <- peConnectService.validateAccessToken(accessTokenResponse)
      recruteurInfos <- peConnectService.getInfosRecruteur(accessTokenResponse.accessToken)
      optRecruteurPEConnnect <- peConnectService.findRecruteur(recruteurInfos.peConnectId)
      optRecruteur <- optRecruteurPEConnnect.map(r => recruteurQueryHandler.getRecruteur(GetRecruteurQuery(r.recruteurId)).map(Some(_))).getOrElse(Future.successful(None))
      recruteurId <- optRecruteurPEConnnect.map(r => mettreAJour(r, recruteurInfos)).getOrElse(inscrire(recruteurInfos))
    } yield {
      val recruteurAuthentifie = RecruteurAuthentifie(
        recruteurId = recruteurId,
        nom = recruteurInfos.nom,
        prenom = recruteurInfos.prenom
      )
      val session = SessionRecruteurPEConnect.set(accessTokenResponse.idToken, SessionRecruteurAuthentifie.set(recruteurAuthentifie, oauthTokenSessionStorage.remove(request.session)))
      // FIXME : en attente de la finalisation du matching
      /**if (optRecruteur.exists(_.profilComplet))
        Redirect(routes.MatchingController.rechercherCandidats()).withSession(session)*/
      if (optRecruteur.isDefined)
        Redirect(routes.ProfilController.modificationProfil()).withSession(session)
      else
        Redirect(routes.ProfilController.modificationProfil()).withSession(session)
          .flashing(request.flash.withRecruteurInscrit)
    }).recover {
      case t: Throwable =>
        Logger.error("Erreur lors de l'authentification PEConnect", t)
        // Nettoyage de session et redirect
        Redirect(routes.LandingController.landing()).withSession(
          oauthTokenSessionStorage.remove(request.session)
        )
    }
  }

  def deconnexion(): Action[AnyContent] = recruteurPEConnectAction.async { implicit request: RecruteurPEConnectRequest[AnyContent] =>
    peConnectService.deconnexionRecruteur(
      idToken = request.idTokenPEConnect,
      redirectUri = routes.LandingController.landing().absoluteURL()
    ).map { _ =>
      // Nettoyage de session et redirect
      Redirect(routes.LandingController.landing()).withSession(
        SessionRecruteurAuthentifie.remove(SessionRecruteurPEConnect.remove(request.session))
      )
    }.recover {
      case t: Throwable =>
        Logger.error("Erreur lors de la déconnexion PEConnect", t)
        // Nettoyage de session et redirect
        Redirect(routes.LandingController.landing()).withSession(
          SessionRecruteurAuthentifie.remove(SessionRecruteurPEConnect.remove(request.session))
        )
    }
  }

  private def inscrire(peConnectRecruteurInfos: PEConnectRecruteurInfos): Future[RecruteurId] = {
    val recruteurId = recruteurCommandHandler.newRecruteurId
    val command = InscrireRecruteurCommand(
      id = recruteurId,
      nom = peConnectRecruteurInfos.nom,
      prenom = peConnectRecruteurInfos.prenom,
      email = peConnectRecruteurInfos.email,
      genre = peConnectRecruteurInfos.genre
    )
    recruteurCommandHandler.inscrire(command)
      .flatMap(_ => peConnectService.saveRecruteur(RecruteurPEConnect(
        recruteurId = recruteurId,
        peConnectId = peConnectRecruteurInfos.peConnectId
      )))
      .map(_ => recruteurId)
  }

  private def mettreAJour(recruteurPEConnect: RecruteurPEConnect,
                          peConnectRecruteurInfos: PEConnectRecruteurInfos): Future[RecruteurId] = {
    val recruteurId = recruteurPEConnect.recruteurId
    val command = ModifierProfilPEConnectCommand(
      id = recruteurId,
      nom = peConnectRecruteurInfos.nom,
      prenom = peConnectRecruteurInfos.prenom,
      email = peConnectRecruteurInfos.email,
      genre = peConnectRecruteurInfos.genre
    )
    recruteurCommandHandler.modifierProfilPEConnect(command).map(_ => recruteurId)
  }
}
