package domain.services

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}
import play.api.libs.ws.WSClient

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class AccessTokenResponse(accessToken: String,
                               idToken: String,
                               nonce: String)

object AccessTokenResponse {

  implicit val accessTokenResponseReads: Reads[AccessTokenResponse] = (
    (JsPath \ "access_token").read[String] and
      (JsPath \ "id_token").read[String] and
      (JsPath \ "nonce").read[String]
    ) (AccessTokenResponse.apply _)
}

case class PEConnectConfig(url: String,
                           clientId: String,
                           clientSecret: String)

class PEConnectService(wsClient: WSClient,
                       peConnectConfig: PEConnectConfig) {

  def getAccessToken(authorizationCode: String,
                     redirectUri: String): Future[AccessTokenResponse] =
    wsClient
      .url(s"${peConnectConfig.url}/connexion/oauth2/access_token?realm=%2Findividu")
      .post(Map(
        "grant_type" -> "authorization_code",
        "code" -> authorizationCode,
        "client_id" -> peConnectConfig.clientId,
        "client_secret" -> peConnectConfig.clientSecret,
        "redirect_uri" -> redirectUri
      )).map(response => {
      if (response.status >= 400) {
        throw new RuntimeException(s"Error lors de l'appel à la generation du token pour PEConnect. Code: ${response.status}. Reponse : ${response.body}")
      } else if (response.status != 200) {
        throw new RuntimeException(s"Statut non géré lors de l'appel à la generation du token pour PEConnect. Code: ${response.status}. Reponse : ${response.body}")
      } else {
        response.json.as[AccessTokenResponse]
      }
    })

  def validateAccessToken(accessTokenResponse: AccessTokenResponse): Future[Unit] = {
    // TODO : valider accessToken + idToken (claims) dés qu'on peut accéder à la clé publique de l'API PEConnect
    Future.successful(())
  }

  def logout(idToken: String,
             redirectUri: String): Future[Unit] = {
    wsClient
      .url(s"${peConnectConfig.url}/compte/deconnexion")
      .addQueryStringParameters(
        ("id_token_hint", idToken),
        ("redirect_uri", redirectUri: String)
      )
      .get()
      .map(response => {
        if (response.status >= 400) {
          throw new RuntimeException(s"Error lors de l'appel à la deconnexion PEConnect. Code: ${response.status}. Reponse : ${response.body}")
        } else if (response.status != 200) {
          throw new RuntimeException(s"Statut non géré lors de l'appel à la deconnexion PEConnect. Code: ${response.status}. Reponse : ${response.body}")
        }
      })
  }

}
