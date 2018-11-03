package fr.poleemploi.perspectives.authentification.infra.peconnect.ws

import fr.poleemploi.perspectives.candidat.{Adresse, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.infra.oauth.OauthConfig
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class PEConnectException(message: String) extends Exception(message)

class PEConnectWSAdapter(wsClient: WSClient,
                         config: PEConnectWSAdapterConfig,
                         recruteurOauthConfig: OauthConfig,
                         candidatOauthConfig: OauthConfig) {

  def getInfosRecruteur(accessToken: AccessToken): Future[PEConnectRecruteurInfos] =
    wsClient
      .url(s"${config.urlApi}/peconnect-entreprise/v1/userinfo")
      .addHttpHeaders(authorizationHeader(accessToken))
      .get()
      .map(filtreStatutReponse(_))
      .map(_.json.as[UserInfosEntrepriseResponse].toPEConnectRecruteurInfos)

  def getInfosCandidat(accessToken: AccessToken): Future[PEConnectCandidatInfos] =
    wsClient
      .url(s"${config.urlApi}/peconnect-individu/v1/userinfo")
      .addHttpHeaders(authorizationHeader(accessToken))
      .get()
      .map(filtreStatutReponse(_))
      .map(_.json.as[UserInfosResponse].toPEConnectCandidatInfos)

  def getCoordonneesCandidat(accessToken: AccessToken): Future[Adresse] =
    wsClient
      .url(s"${config.urlApi}/peconnect-coordonnees/v1/coordonnees")
      .addHttpHeaders(authorizationHeader(accessToken))
      .get()
      .map(filtreStatutReponse(_))
      .map(_.json.as[CoordonneesCandidatReponse].toAdresse)

  def getStatutDemandeurEmploiCandidat(accessToken: AccessToken): Future[StatutDemandeurEmploi] =
    wsClient
      .url(s"${config.urlApi}/peconnect-statut/v1/statut")
      .addHttpHeaders(authorizationHeader(accessToken))
      .get()
      .map(filtreStatutReponse(_))
      .map(_.json.as[StatutCandidatReponse].toStatutDemandeurEmploi)

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
      .map(filtreStatutReponse(_))
      .map(_.json.as[AccessTokenResponse])

  private def filtreStatutReponse(response: WSResponse,
                                  statutErreur: Int => Boolean = s => s >= 400,
                                  statutNonGere: Int => Boolean = s => s != 200): WSResponse = response.status match {
    case s if statutErreur(s) => throw PEConnectException(s"Erreur lors de l'appel à PEConnect. Code: ${response.status}. Reponse : ${response.body}")
    case s if statutNonGere(s) => throw PEConnectException(s"Statut non géré lors de l'appel à PEConnect. Code: ${response.status}. Reponse : ${response.body}")
    case _ => response
  }

  private def authorizationHeader(accessToken: AccessToken): (String, String) = ("Authorization", s"Bearer ${accessToken.value}")
}