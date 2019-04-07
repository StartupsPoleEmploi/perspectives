package fr.poleemploi.perspectives.authentification.infra.peconnect

import fr.poleemploi.perspectives.authentification.infra.peconnect.jwt.PEConnectJWTAdapter
import fr.poleemploi.perspectives.authentification.infra.peconnect.ws._
import fr.poleemploi.perspectives.commun.EitherUtils._
import fr.poleemploi.perspectives.commun.infra.oauth.{OauthService, OauthTokens}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PEConnectAuthAdapter(oauthService: OauthService,
                           peConnectAuthWSAdapter: PEConnectAuthWSAdapter,
                           peConnectJWTAdapter: PEConnectJWTAdapter) {

  def generateTokens: OauthTokens =
    oauthService.generateTokens

  def verifyState(oauthTokens: OauthTokens, state: String): Boolean =
    oauthService.verifyState(oauthTokens, state)

  def getAccessTokenCandidat(authorizationCode: String,
                             redirectUri: String,
                             oauthTokens: OauthTokens): Future[AccessTokenResponse] =
    for {
      accessTokenResponse <- peConnectAuthWSAdapter.getAccessTokenCandidat(authorizationCode = authorizationCode, redirectUri = redirectUri)
      _ <- Either.cond(oauthService.verifyNonce(oauthTokens, accessTokenResponse.nonce), (), "La comparaison du nonce a échoué").toFuture
      _ <- peConnectJWTAdapter.validateCandidatToken(accessTokenResponse.idToken, oauthTokens.nonce)
    } yield accessTokenResponse

  def getAccessTokenRecruteur(authorizationCode: String,
                              redirectUri: String,
                              oauthTokens: OauthTokens): Future[AccessTokenResponse] =
    for {
      accessTokenResponse <- peConnectAuthWSAdapter.getAccessTokenRecruteur(authorizationCode = authorizationCode, redirectUri = redirectUri)
      _ <- Either.cond(oauthService.verifyNonce(oauthTokens, accessTokenResponse.nonce), (), "La comparaison du nonce a échoué").toFuture
      _ <- peConnectJWTAdapter.validateRecruteurToken(accessTokenResponse.idToken, oauthTokens.nonce)
    } yield accessTokenResponse
}
