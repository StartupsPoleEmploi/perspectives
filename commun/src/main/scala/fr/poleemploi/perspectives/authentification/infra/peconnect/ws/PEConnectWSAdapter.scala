package fr.poleemploi.perspectives.authentification.infra.peconnect.ws

import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.candidat.{Adresse, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.infra.oauth.OauthConfig
import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// FIXME : ce n'est pas que de l'authentification
class PEConnectWSAdapter(wsClient: WSClient,
                         config: PEConnectWSAdapterConfig,
                         mapping: PEConnectWSMapping,
                         recruteurOauthConfig: OauthConfig,
                         candidatOauthConfig: OauthConfig) extends WSAdapter {

  def getPrestationsCandidat(accessToken: AccessToken): Future[List[MRSValidee]] =
    wsClient
      .url(s"${config.urlApi}/prestationssuivies/v1/resultat/rendez-vous?listeCodeTypePrestation=ESP&listeCodeTypePrestation=ESPR")
      .addHttpHeaders(authorizationHeader(accessToken))
      .get()
      .flatMap(filtreStatutReponse(_))
      .map(_.json.as[List[ResultatRendezVousResponse]].flatMap(mapping.buildMRSValidee))

  def getInfosRecruteur(accessToken: AccessToken): Future[PEConnectRecruteurInfos] =
    wsClient
      .url(s"${config.urlApi}/peconnect-entreprise/v1/userinfo")
      .addHttpHeaders(authorizationHeader(accessToken))
      .get()
      .flatMap(filtreStatutReponse(_))
      .map(r => mapping.buildPEConnectRecruteurInfos(r.json.as[UserInfosEntrepriseResponse]))

  def getInfosCandidat(accessToken: AccessToken): Future[PEConnectCandidatInfos] =
    wsClient
      .url(s"${config.urlApi}/peconnect-individu/v1/userinfo")
      .addHttpHeaders(authorizationHeader(accessToken))
      .get()
      .flatMap(filtreStatutReponse(_))
      .map(r => mapping.buildPEConnectCandidatInfos(r.json.as[UserInfosResponse]))

  def getCoordonneesCandidat(accessToken: AccessToken): Future[Adresse] =
    wsClient
      .url(s"${config.urlApi}/peconnect-coordonnees/v1/coordonnees")
      .addHttpHeaders(authorizationHeader(accessToken))
      .get()
      .flatMap(filtreStatutReponse(_))
      .map(r => mapping.buildAdresse(r.json.as[CoordonneesCandidatReponse]))

  def getStatutDemandeurEmploiCandidat(accessToken: AccessToken): Future[StatutDemandeurEmploi] =
    wsClient
      .url(s"${config.urlApi}/peconnect-statut/v1/statut")
      .addHttpHeaders(authorizationHeader(accessToken))
      .get()
      .flatMap(filtreStatutReponse(_))
      .map(r => mapping.buildStatutDemandeurEmploi(r.json.as[StatutCandidatReponse]))

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

  private def authorizationHeader(accessToken: AccessToken): (String, String) = ("Authorization", s"Bearer ${accessToken.value}")
}