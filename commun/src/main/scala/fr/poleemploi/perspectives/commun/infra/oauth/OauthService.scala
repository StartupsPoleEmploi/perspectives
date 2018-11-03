package fr.poleemploi.perspectives.commun.infra.oauth

trait OauthService {

  def generateTokens: OauthTokens

  def verifyState(oauthTokens: OauthTokens, state: String): Boolean

  def verifyNonce(oauthTokens: OauthTokens, nonce: String): Boolean
}
