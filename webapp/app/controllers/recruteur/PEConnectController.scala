package controllers.recruteur

import java.util.UUID

import authentification.infra.peconnect._
import authentification.infra.play._
import authentification.model.RecruteurAuthentifie
import conf.WebAppConfig
import fr.poleemploi.perspectives.domain.recruteur.{InscrireRecruteurCommand, RecruteurCommandHandler, RecruteurId}
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
                                    peConnectFacade: PEConnectFacade,
                                    recruteurCommandHandler: RecruteurCommandHandler) extends AbstractController(cc) {

  val oauthTokenSessionStorage = new OauthTokenSessionStorage("recruteur")
  val redirectUri: Call = routes.PEConnectController.connexionCallback()
  val peConnectConfig: PEConnectRecruteurConfig = webAppConfig.peConnectRecruteurConfig

  def inscription(): Action[AnyContent] = Action { request =>
    Redirect(routes.PEConnectController.connexion()).withSession(
      oauthTokenSessionStorage.set(peConnectFacade.generateTokens(), request.session)
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
      accessTokenResponse <- peConnectFacade.getAccessTokenRecruteur(
        authorizationCode = authorizationCode,
        redirectUri = redirectUri.absoluteURL()
      )
      _ <- Either.cond(peConnectFacade.verifyState(oauthTokens, stateCallback), (), "La comparaison du state a échoué").toFuture
      _ <- Either.cond(peConnectFacade.verifyNonce(oauthTokens, accessTokenResponse.nonce), (), "La comparaison du nonce a échoué").toFuture
      _ <- peConnectFacade.validateAccessToken(accessTokenResponse)
      recruteurInfos <- peConnectFacade.getInfosRecruteur(accessTokenResponse.accessToken)
      recruteurId <- inscrire(recruteurInfos)
    } yield {
      val recruteurAuthentifie = RecruteurAuthentifie(
        recruteurId = recruteurId.value,
        nom = recruteurInfos.nom,
        prenom = recruteurInfos.prenom
      )
      Redirect(routes.ProfilController.modificationProfil())
        .withSession(SessionRecruteurPEConnect.set(accessTokenResponse.idToken, SessionRecruteurAuthentifie.set(recruteurAuthentifie, oauthTokenSessionStorage.remove(request.session))))
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
    peConnectFacade.deconnexionRecruteur(
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
    for {
      optRecruteur <- peConnectFacade.findRecruteur(peConnectRecruteurInfos.peConnectId)
      recruteurId <- optRecruteur.map(recruteur => Future(RecruteurId(recruteur.recruteurId)))
        .getOrElse {
          val recruteurId = RecruteurId(UUID.randomUUID().toString)
          val command = InscrireRecruteurCommand(
            id = recruteurId,
            nom = peConnectRecruteurInfos.nom,
            prenom = peConnectRecruteurInfos.prenom,
            email = peConnectRecruteurInfos.email,
            genre = peConnectRecruteurInfos.genre
          )
          recruteurCommandHandler.inscrire(command)
            .flatMap(_ => peConnectFacade.saveRecruteur(RecruteurPEConnect(
              recruteurId = recruteurId.value,
              peConnectId = peConnectRecruteurInfos.peConnectId
            )))
            .map(_ => recruteurId)
        }
    } yield recruteurId
  }
}
