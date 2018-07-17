package authentification.infra.peconnect

import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PEConnectWS(wsClient: WSClient,
                  peConnectRecruteurConfig: PEConnectRecruteurConfig,
                  peConnectCandidatConfig: PEConnectCandidatConfig) {

  def getInfosRecruteur(accessToken: String): Future[PEConnectRecruteurInfos] =
    wsClient
      .url(s"${peConnectRecruteurConfig.urlApi}")
      .addHttpHeaders(("Authorization", s"Bearer $accessToken"))
      .get()
      .map(filtreStatutReponse(_))
      .map(response =>
        RecruteurUserInfos.toPEConnectRecruteurInfos(response.json.as[RecruteurUserInfos])
      )

  def getInfosCandidat(accessToken: String): Future[PEConnectCandidatInfos] =
    wsClient
      .url(s"${peConnectCandidatConfig.urlApi}")
      .addHttpHeaders(("Authorization", s"Bearer $accessToken"))
      .get()
      .map(filtreStatutReponse(_))
      .map(response =>
        CandidatUserInfos.toPEConnectCandidatInfos(response.json.as[CandidatUserInfos])
      )

  def getAccessTokenCandidat(authorizationCode: String,
                             redirectUri: String): Future[AccessTokenResponse] =
    wsClient
      .url(s"${peConnectCandidatConfig.urlAuthentification}/connexion/oauth2/access_token?realm=%2Findividu")
      .post(Map(
        "grant_type" -> "authorization_code",
        "code" -> authorizationCode,
        "client_id" -> peConnectCandidatConfig.clientId,
        "client_secret" -> peConnectCandidatConfig.clientSecret,
        "redirect_uri" -> redirectUri
      ))
      .map(filtreStatutReponse(_))
      .map(_.json.as[AccessTokenResponse])

  def getAccessTokenRecruteur(authorizationCode: String,
                              redirectUri: String): Future[AccessTokenResponse] =
    wsClient
      .url(s"${peConnectRecruteurConfig.urlAuthentification}/connexion/oauth2/access_token?realm=%2Femployeur")
      .post(Map(
        "grant_type" -> "authorization_code",
        "code" -> authorizationCode,
        "client_id" -> peConnectRecruteurConfig.clientId,
        "client_secret" -> peConnectRecruteurConfig.clientSecret,
        "redirect_uri" -> redirectUri
      ))
      .map(filtreStatutReponse(_))
      .map(_.json.as[AccessTokenResponse])

  def deconnexionCandidat(idToken: String,
                          redirectUri: String): Future[Unit] =
    wsClient
      .url(s"${peConnectCandidatConfig.urlAuthentification}/compte/deconnexion")
      .addQueryStringParameters(
        ("id_token_hint", idToken),
        ("redirect_uri", redirectUri: String)
      )
      .get()
      .map(filtreStatutReponse(_))

  def deconnexionRecruteur(idToken: String,
                           redirectUri: String): Future[Unit] =
    wsClient
      .url(s"${peConnectRecruteurConfig.urlAuthentification}/compte/deconnexion")
      .addQueryStringParameters(
        ("id_token_hint", idToken),
        ("redirect_uri", redirectUri: String)
      )
      .get()
      .map(filtreStatutReponse(_))

  private def filtreStatutReponse(response: WSResponse,
                                  statutErreur: Int => Boolean = s => s >= 400,
                                  statutNonGere: Int => Boolean = s => s != 200): WSResponse = response.status match {
    case s if statutErreur(s) => throw new RuntimeException(s"Erreur lors de l'appel à PEConnect. Code: ${response.status}. Reponse : ${response.body}")
    case s if statutNonGere(s) => throw new RuntimeException(s"Statut non géré lors de l'appel à PEConnect. Code: ${response.status}. Reponse : ${response.body}")
    case _ => response
  }
}