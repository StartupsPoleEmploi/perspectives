package authentification.infra.peconnect

import play.filters.csrf.CSRF.TokenProvider

case class OauthTokens(state: String, nonce: String)

class OauthService(tokenProvider: TokenProvider) {

  def generateTokens(): OauthTokens =
    OauthTokens(
      state = tokenProvider.generateToken,
      nonce = tokenProvider.generateToken
    )

  def verifyState(oauthTokens: OauthTokens, state: String): Boolean =
    tokenProvider.compareTokens(oauthTokens.state, state)

  def verifyNonce(oauthTokens: OauthTokens, nonce: String): Boolean =
    tokenProvider.compareTokens(oauthTokens.nonce, nonce)

}
