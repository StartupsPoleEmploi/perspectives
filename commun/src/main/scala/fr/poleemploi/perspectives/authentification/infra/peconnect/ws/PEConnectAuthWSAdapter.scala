package fr.poleemploi.perspectives.authentification.infra.peconnect.ws

import fr.poleemploi.perspectives.commun.infra.oauth.OauthConfig
import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PEConnectAuthWSAdapter(wsClient: WSClient,
                             recruteurOauthConfig: OauthConfig,
                             candidatOauthConfig: OauthConfig) extends WSAdapter {

  def getAccessTokenCandidat(authorizationCode: String,
                             redirectUri: String): Future[AccessTokenResponse] =
    getAccessToken(
      authorizationCode = authorizationCode,
      redirectUri = redirectUri,
      oauthConfig = candidatOauthConfig
    )

  def getAccessTokenRecruteur(authorizationCode: String,
                              redirectUri: String): Future[AccessTokenResponse] =
    getAccessToken(
      authorizationCode = authorizationCode,
      redirectUri = redirectUri,
      oauthConfig = recruteurOauthConfig
    )

  private def getAccessToken(authorizationCode: String,
                             redirectUri: String,
                             oauthConfig: OauthConfig): Future[AccessTokenResponse] =
    wsClient
      .url(s"${oauthConfig.urlAuthentification}/connexion/oauth2/access_token?realm=%2F${oauthConfig.realm}")
      .post(Map(
        "grant_type" -> "authorization_code",
        "code" -> authorizationCode,
        "client_id" -> oauthConfig.clientId,
        "client_secret" -> oauthConfig.clientSecret,
        "redirect_uri" -> redirectUri
      ))
      .flatMap(filtreStatutReponse(_))
      .map(_.json.as[AccessTokenResponse])
}