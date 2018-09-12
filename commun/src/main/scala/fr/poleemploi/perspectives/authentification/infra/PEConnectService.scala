package fr.poleemploi.perspectives.authentification.infra

import fr.poleemploi.perspectives.authentification.infra.sql.{CandidatPEConnect, PEConnectSqlAdapter, RecruteurPEConnect}
import fr.poleemploi.perspectives.authentification.infra.ws.{AccessTokenResponse, PEConnectCandidatInfos, PEConnectRecruteurInfos, PEConnectWSAdapter}
import fr.poleemploi.perspectives.candidat.{Adresse, CandidatId, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.infra.oauth.{OauthService, OauthTokens}
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId

import scala.concurrent.Future

/**
  * Façade pour gérer les interactions complexes avec PEConnect (tokens, verification d'un candidat existant, appels WS)
  * FIXME : Séparer en ConnexionService et InscriptionService
  */
class PEConnectService(oauthService: OauthService,
                       peConnectWSAdapter: PEConnectWSAdapter,
                       peConnectSqlAdapter: PEConnectSqlAdapter) {

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
    peConnectSqlAdapter.findCandidat(peConnectId)

  def getCandidat(candidatId: CandidatId): Future[CandidatPEConnect] =
    peConnectSqlAdapter.getCandidat(candidatId)

  def saveCandidat(candidatPEConnect: CandidatPEConnect): Future[Unit] =
    peConnectSqlAdapter.saveCandidat(candidatPEConnect)

  def findRecruteur(peConnectId: PEConnectId): Future[Option[RecruteurPEConnect]] =
    peConnectSqlAdapter.findRecruteur(peConnectId)

  def saveRecruteur(recruteurPEConnect: RecruteurPEConnect): Future[Unit] =
    peConnectSqlAdapter.saveRecruteur(recruteurPEConnect)

  def getInfosRecruteur(accessToken: String): Future[PEConnectRecruteurInfos] =
    peConnectWSAdapter.getInfosRecruteur(accessToken)

  def getInfosCandidat(accessToken: String): Future[PEConnectCandidatInfos] =
    peConnectWSAdapter.getInfosCandidat(accessToken)

  def getAdresseCandidat(accessToken: String): Future[Adresse] =
    peConnectWSAdapter.getCoordonneesCandidat(accessToken)

  def getStatutDemandeurEmploiCandidat(accessToken: String): Future[StatutDemandeurEmploi] =
    peConnectWSAdapter.getStatutDemandeurEmploiCandidat(accessToken)

  def getAccessTokenCandidat(authorizationCode: String,
                             redirectUri: String): Future[AccessTokenResponse] =
    peConnectWSAdapter.getAccessTokenCandidat(authorizationCode = authorizationCode, redirectUri = redirectUri)

  def getAccessTokenRecruteur(authorizationCode: String,
                              redirectUri: String): Future[AccessTokenResponse] =
    peConnectWSAdapter.getAccessTokenRecruteur(authorizationCode = authorizationCode, redirectUri = redirectUri)

  def deconnexionCandidat(idToken: String,
                          redirectUri: String): Future[Unit] =
    peConnectWSAdapter.deconnexionCandidat(idToken = idToken, redirectUri = redirectUri)

  def deconnexionRecruteur(idToken: String,
                           redirectUri: String): Future[Unit] =
    peConnectWSAdapter.deconnexionRecruteur(idToken = idToken, redirectUri = redirectUri)
}
