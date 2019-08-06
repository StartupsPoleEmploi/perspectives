package fr.poleemploi.perspectives.authentification.infra.peconnect

import fr.poleemploi.perspectives.authentification.infra.peconnect.jwt.PEConnectJWTAdapter
import fr.poleemploi.perspectives.authentification.infra.peconnect.ws._
import fr.poleemploi.perspectives.commun.EitherUtils._
import fr.poleemploi.perspectives.commun.infra.oauth.{OauthConfig, OauthService, OauthTokens}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PEConnectAuthAdapter(oauthService: OauthService,
                           peConnectAuthWSAdapter: PEConnectAuthWSAdapter,
                           peConnectJWTAdapter: PEConnectJWTAdapter) {

  def generateTokens: OauthTokens = oauthService.generateTokens

  def accessToken(authorizationCode: String,
                  state: String,
                  redirectUri: String,
                  oauthTokens: OauthTokens,
                  oauthConfig: OauthConfig): Future[AccessTokenResponse] =
    for {
      accessTokenResponse <- peConnectAuthWSAdapter.accessToken(authorizationCode, redirectUri, oauthConfig)
      _ <- Either.cond(oauthService.verifyState(oauthTokens, state), (), "La comparaison du state a échoué").toFuture
      _ <- Either.cond(oauthService.verifyNonce(oauthTokens, accessTokenResponse.nonce), (), "La comparaison du nonce a échoué").toFuture
      _ <- peConnectJWTAdapter.validate(
        jwtToken = accessTokenResponse.idToken,
        nonce = oauthTokens.nonce,
        oauthConfig = oauthConfig
      )
    } yield accessTokenResponse
}
