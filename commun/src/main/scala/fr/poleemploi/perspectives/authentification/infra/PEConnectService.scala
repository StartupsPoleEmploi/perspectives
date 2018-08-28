package fr.poleemploi.perspectives.authentification.infra

import fr.poleemploi.perspectives.authentification.infra.sql.{CandidatPEConnect, PEConnectSqlAdapter, RecruteurPEConnect}
import fr.poleemploi.perspectives.authentification.infra.ws.{AccessTokenResponse, PEConnectCandidatInfos, PEConnectRecruteurInfos, PEConnectWSAdapter}
import fr.poleemploi.perspectives.candidat.{Adresse, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.infra.oauth.{OauthService, OauthTokens}
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId

import scala.concurrent.Future

/**
  * Façade pour gérer les interactions complexes avec PEConnect (tokens, verification d'un candidat existant, appels WS)
  */
class PEConnectService(oauthService: OauthService,
                       peConnectWS: PEConnectWSAdapter,
                       peConnectInscrisService: PEConnectSqlAdapter) {

  def generateTokens(): OauthTokens =
    oauthService.generateTokens()

  def verifyState(oauthTokens: OauthTokens, state: String): Boolean =
    oauthService.verifyState(oauthTokens, state)

  def verifyNonce(oauthTokens: OauthTokens, nonce: String): Boolean =
    oauthService.verifyNonce(oauthTokens, nonce)

  def validateAccessToken(accessTokenResponse: AccessTokenResponse): Future[Unit] = {
    // TODO : valider accessToken + idToken (claims) dés qu'on peut accéder à la clé publique de l'API PEConnect
    Future.successful(())
  }

  def findCandidat(peConnectId: PEConnectId): Future[Option[CandidatPEConnect]] =
    peConnectInscrisService.findCandidat(peConnectId)

  def saveCandidat(candidatPEConnect: CandidatPEConnect): Future[Unit] =
    peConnectInscrisService.saveCandidat(candidatPEConnect)

  def findRecruteur(peConnectId: PEConnectId): Future[Option[RecruteurPEConnect]] =
    peConnectInscrisService.findRecruteur(peConnectId)

  def saveRecruteur(recruteurPEConnect: RecruteurPEConnect): Future[Unit] =
    peConnectInscrisService.saveRecruteur(recruteurPEConnect)

  def getInfosRecruteur(accessToken: String): Future[PEConnectRecruteurInfos] =
    peConnectWS.getInfosRecruteur(accessToken)

  def getInfosCandidat(accessToken: String): Future[PEConnectCandidatInfos] =
    peConnectWS.getInfosCandidat(accessToken)

  def getAdresseCandidat(accessToken: String): Future[Adresse] =
    peConnectWS.getCoordonneesCandidat(accessToken)

  def getStatutDemandeurEmploiCandidat(accessToken: String): Future[StatutDemandeurEmploi] =
    peConnectWS.getStatutDemandeurEmploiCandidat(accessToken)

  def getAccessTokenCandidat(authorizationCode: String,
                             redirectUri: String): Future[AccessTokenResponse] =
    peConnectWS.getAccessTokenCandidat(authorizationCode = authorizationCode, redirectUri = redirectUri)

  def getAccessTokenRecruteur(authorizationCode: String,
                              redirectUri: String): Future[AccessTokenResponse] =
    peConnectWS.getAccessTokenRecruteur(authorizationCode = authorizationCode, redirectUri = redirectUri)

  def deconnexionCandidat(idToken: String,
                          redirectUri: String): Future[Unit] =
    peConnectWS.deconnexionCandidat(idToken = idToken, redirectUri = redirectUri)

  def deconnexionRecruteur(idToken: String,
                           redirectUri: String): Future[Unit] =
    peConnectWS.deconnexionRecruteur(idToken = idToken, redirectUri = redirectUri)
}
