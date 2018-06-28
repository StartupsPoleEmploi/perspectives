package controllers.candidat

import java.util.UUID

import authentification.infra.peconnect._
import authentification.infra.play._
import authentification.model.CandidatAuthentifie
import conf.WebAppConfig
import fr.poleemploi.eventsourcing.AggregateId
import fr.poleemploi.perspectives.domain.candidat.{CandidatCommandHandler, InscrireCandidatCommand}
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
                                    peConnectFacade: PEConnectFacade) extends AbstractController(cc) {

  val oauthTokenSessionStorage = new OauthTokenSessionStorage("candidat")
  val redirectUri: Call = routes.PEConnectController.connexionCallback()
  val peConnectConfig: PEConnectCandidatConfig = webAppConfig.peConnectCandidatConfig

  def inscription(): Action[AnyContent] = Action { request =>
    Redirect(routes.PEConnectController.connexion()).withSession(
      oauthTokenSessionStorage.set(peConnectFacade.generateTokens(), request.session)
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
            "scope" -> Seq(s"application_${peConnectConfig.clientId} api_peconnect-individuv1 openid profile email"),
            "redirect_uri" -> Seq(redirectUri.absoluteURL()),
            "state" -> Seq(oauthTokens.state),
            "nonce" -> Seq(oauthTokens.nonce)
          )
        )).recover {
      case t: Throwable =>
        Logger.error("Erreur lors de l'authentification PEConnect", t)
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
      accessTokenResponse <- peConnectFacade.getAccessTokenCandidat(
        authorizationCode = authorizationCode,
        redirectUri = redirectUri.absoluteURL()
      )
      _ <- Either.cond(peConnectFacade.verifyState(oauthTokens, stateCallback), (), "La comparaison du state a échoué").toFuture
      _ <- Either.cond(peConnectFacade.verifyNonce(oauthTokens, accessTokenResponse.nonce), (), "La comparaison du nonce a échoué").toFuture
      _ <- peConnectFacade.validateAccessToken(accessTokenResponse)
      candidatInfos <- peConnectFacade.getInfosCandidat(accessTokenResponse.accessToken)
      aggregateId <- inscrire(candidatInfos)
    } yield {
      val candidatAuthentifie = CandidatAuthentifie(
        candidatId = aggregateId.value,
        nom = candidatInfos.nom,
        prenom = candidatInfos.prenom
      )
      Redirect(routes.SaisieCriteresRechercheController.saisieCriteresRecherche())
        .withSession(SessionCandidatPEConnect.set(accessTokenResponse.idToken, SessionCandidatAuthentifie.set(candidatAuthentifie, oauthTokenSessionStorage.remove(request.session))))
    }).recover {
      case t: Throwable =>
        Logger.error("Erreur lors du callback PEConnect", t)
        // Nettoyage de session et redirect
        Redirect(routes.LandingController.landing()).withSession(
          oauthTokenSessionStorage.remove(request.session)
        )
    }
  }

  def deconnexion(): Action[AnyContent] = candidatPEConnectAction.async { implicit request: CandidatPEConnectRequest[AnyContent] =>
    peConnectFacade.deconnexionCandidat(
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

  private def inscrire(peConnectCandidatInfos: PEConnectCandidatInfos): Future[AggregateId] =
    for {
      optCandidat <- peConnectFacade.findCandidat(peConnectCandidatInfos.peConnectId)
      aggregateId <- optCandidat.map(candidat => Future(AggregateId(candidat.candidatId)))
        .getOrElse {
          val aggregateId = AggregateId(UUID.randomUUID().toString)
          val command = InscrireCandidatCommand(
            id = aggregateId,
            nom = peConnectCandidatInfos.nom,
            prenom = peConnectCandidatInfos.prenom,
            genre = peConnectCandidatInfos.genre,
            email = peConnectCandidatInfos.email
          )
          candidatCommandHandler.inscrire(command)
            .flatMap(_ => peConnectFacade.saveCandidat(CandidatPEConnect(
              candidatId = aggregateId.value,
              peConnectId = peConnectCandidatInfos.peConnectId
            )))
            .map(_ => aggregateId)
        }
    } yield aggregateId
}
