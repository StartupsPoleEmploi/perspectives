package controllers

import java.util.UUID

import authentification.{AuthenticatedAction, AuthenticatedCandidat, CandidatRequest}
import conf.WebAppConfig
import domain.services._
import fr.poleemploi.eventsourcing.AggregateId
import fr.poleemploi.perspectives.domain.candidat.{CandidatCommandHandler, InscrireCandidatCommand}
import fr.poleemploi.perspectives.projections.{CandidatQueryHandler, FindCandidatQuery}
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.mvc._
import play.filters.csrf.CSRF.TokenProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AuthentificationController @Inject()(cc: ControllerComponents,
                                           tokenProvider: TokenProvider,
                                           webappConfig: WebAppConfig,
                                           withAuthentificationAction: AuthenticatedAction,
                                           peConnectService: PEConnectService,
                                           peConnectIndividuService: PEConnectIndividuService,
                                           commandHandler: CandidatCommandHandler,
                                           queryHandler: CandidatQueryHandler) extends AbstractController(cc) {

  val stateAttribute: String = "oauth.state"
  val nonceAttribute: String = "oauth.nonce"
  val redirectUri: Call = routes.AuthentificationController.connexionCallback()

  def connexionPEConnect(): Action[AnyContent] =
    if (webappConfig.usePEConnect) {
      Action { request =>
          val stateToken = tokenProvider.generateToken
          val nonceToken = tokenProvider.generateToken

          Redirect(routes.AuthentificationController.connexion()).withSession(
            request.session + (stateAttribute -> stateToken) + (nonceAttribute -> nonceToken)
          )
      }
    } else connexionSimple()

  def connexion(): Action[AnyContent] = Action.async { implicit request =>
    (for {
      stateToken <- Future(request.session.get(stateAttribute).getOrElse(
        throw new IllegalArgumentException("Erreurs lors du login : L'attribut state n'existe pas"))
      )
      nonceToken <- Future(request.session.get(nonceAttribute).getOrElse {
        throw new IllegalArgumentException("Erreurs lors du login : L'attribut nonce n'existe pas")
      })
    } yield {
      Redirect(
        url = s"${webappConfig.peConnectConfig.url}/connexion/oauth2/authorize",
        status = SEE_OTHER,
        queryString = Map(
          "realm" -> Seq("/individu"),
          "response_type" -> Seq("code"),
          "client_id" -> Seq(webappConfig.peConnectConfig.clientId),
          "scope" -> Seq(s"application_${webappConfig.peConnectConfig.clientId} api_peconnect-individuv1 openid profile email"),
          "redirect_uri" -> Seq(redirectUri.absoluteURL()),
          "state" -> Seq(stateToken),
          "nonce" -> Seq(nonceToken)
        )
      )
    }).recover {
      case t: Throwable =>
        Logger.error("Erreur lors de l'authentification PEConnect", t)
        // Clear login session and redirect
        Redirect(routes.LandingController.landing()).withSession(
          request.session - stateAttribute - nonceAttribute
        )
    }
  }

  def connexionCallback(): Action[AnyContent] = Action.async { implicit request =>
    (for {
      authorizationCode <- Future(request.getQueryString("code").getOrElse(
        throw new IllegalArgumentException("Erreurs lors du loginCallback: aucun code d'autorisation n'a été retourné")
      ))
      stateCallback <- Future(request.getQueryString("state").getOrElse(
        throw new IllegalArgumentException("Erreurs lors du loginCallback: aucun state n'a été retourné")
      ))
      stateSession <- Future(request.session.get(stateAttribute).getOrElse(
        throw new IllegalArgumentException("Erreurs lors du loginCallback: aucun state n'a été stocké")
      ))
      nonceSession <- Future(request.session.get(nonceAttribute).getOrElse(
        throw new IllegalArgumentException("Erreurs lors du loginCallback: aucun nonce n'a été stocké")
      ))
      _ <- Future(if (!tokenProvider.compareTokens(stateSession, stateCallback))
        throw new IllegalArgumentException("Erreurs lors du loginCallback: La comparaison du state a échoué")
      )
      accessTokenResponse <- peConnectService.getAccessToken(
        authorizationCode = authorizationCode,
        redirectUri = redirectUri.absoluteURL()
      )
      _ <- validerAccessToken(nonceSession, accessTokenResponse)
      userInfos <- peConnectIndividuService.getUserInfos(accessTokenResponse.accessToken)
      aggregateId <- inscrire(userInfos)
    } yield AuthenticatedCandidat(
      candidatId = aggregateId.value,
      idTokenPEConnect = Some(accessTokenResponse.idToken),
      nom = userInfos.nom,
      prenom = userInfos.prenom
    )).map { c =>
      Redirect(routes.SaisieCriteresRechercheController.saisieCriteresRecherche())
        .withSession(
        AuthenticatedCandidat.storeInSession(c, request.session) - stateAttribute - nonceAttribute
      )
    }.recover {
      case t: Throwable =>
        Logger.error("Erreur lors de l'authentification PEConnect", t)
        // Clear login session and redirect
        Redirect(routes.LandingController.landing()).withSession(
          request.session - stateAttribute - nonceAttribute
        )
    }
  }

  def deconnexion(): Action[AnyContent] = withAuthentificationAction.async { implicit request: CandidatRequest[AnyContent] =>
    (for {
      _ <- request.idTokenPEConnect.map(idToken => peConnectService.logout(
        idToken = idToken,
        redirectUri = routes.LandingController.landing().absoluteURL()
      )).getOrElse(Future.successful(()))
    } yield {
      // Clear session and redirect
      Redirect(routes.LandingController.landing()).withNewSession
    }).recover {
      case t: Throwable =>
        Logger.error("Erreur lors de la déconnexion", t)
        // Clear session and redirect
        Redirect(routes.LandingController.landing()).withNewSession
    }
  }

  private def validerAccessToken(nonceSession: String, accessTokenResponse: AccessTokenResponse): Future[Unit] = {
    for {
      _ <- Future(if (!tokenProvider.compareTokens(nonceSession, accessTokenResponse.nonce))
        throw new RuntimeException("Erreurs lors du loginCallback: La comparaison du nonce a échoué"))
    } yield peConnectService.validateAccessToken(accessTokenResponse)
  }

  private def inscrire(userInfos: UserInfos): Future[AggregateId] = {
    queryHandler.findCandidat(FindCandidatQuery(
      peConnectId = userInfos.peConnectId
    )).map(optCandidat => {
      optCandidat
        .map(u => AggregateId(u.candidatId))
        .getOrElse {
          val aggregateId = AggregateId(UUID.randomUUID().toString)
          val command = InscrireCandidatCommand(
            id = aggregateId,
            peConnectId = userInfos.peConnectId,
            nom = userInfos.nom,
            prenom = userInfos.prenom,
            email = userInfos.email
          )
          commandHandler.inscrire(command)
          aggregateId
        }
    })
  }

  private def connexionSimple(): Action[AnyContent] = Action.async { implicit request =>
    val userInfos = UserInfos(
      nom = "robert",
      prenom = "plantu",
      email = "robert.plantu@mail.com",
      peConnectId = UUID.randomUUID().toString
    )

    inscrire(userInfos).map { aggregateId =>
      val authenticatedCandidat = AuthenticatedCandidat(
        candidatId = aggregateId.value,
        idTokenPEConnect = None,
        nom = userInfos.nom,
        prenom = userInfos.prenom
      )
      Redirect(routes.SaisieCriteresRechercheController.saisieCriteresRecherche())
        .withSession(AuthenticatedCandidat.storeInSession(authenticatedCandidat, request.session))
    }
  }
}
