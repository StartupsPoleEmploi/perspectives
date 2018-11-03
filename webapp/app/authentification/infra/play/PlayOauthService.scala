package authentification.infra.play

import fr.poleemploi.perspectives.commun.infra.oauth.{OauthService, OauthTokens}
import play.filters.csrf.CSRF.TokenProvider

class PlayOauthService(tokenProvider: TokenProvider) extends OauthService {

  def generateTokens: OauthTokens =
    OauthTokens(
      state = tokenProvider.generateToken,
      nonce = tokenProvider.generateToken
    )

  def verifyState(oauthTokens: OauthTokens, state: String): Boolean =
    tokenProvider.compareTokens(oauthTokens.state, state)

  def verifyNonce(oauthTokens: OauthTokens, nonce: String): Boolean =
    tokenProvider.compareTokens(oauthTokens.nonce, nonce)

}
