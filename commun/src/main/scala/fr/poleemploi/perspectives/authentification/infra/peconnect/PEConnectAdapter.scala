package fr.poleemploi.perspectives.authentification.infra.peconnect

import fr.poleemploi.perspectives.authentification.infra.peconnect.jwt.PEConnectJWTAdapter
import fr.poleemploi.perspectives.authentification.infra.peconnect.sql.{CandidatPEConnect, PEConnectSqlAdapter, RecruteurPEConnect}
import fr.poleemploi.perspectives.authentification.infra.peconnect.ws._
import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.candidat.{Adresse, CandidatId, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.EitherUtils._
import fr.poleemploi.perspectives.commun.infra.oauth.{OauthService, OauthTokens}
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Façade pour gérer les interactions complexes avec PEConnect (tokens, verification d'un candidat existant, appels WS)
  */
class PEConnectAdapter(oauthService: OauthService,
                       peConnectWSAdapter: PEConnectWSAdapter,
                       peConnectSqlAdapter: PEConnectSqlAdapter,
                       peConnectJWTAdapter: PEConnectJWTAdapter) {

  def generateTokens: OauthTokens =
    oauthService.generateTokens

  def verifyState(oauthTokens: OauthTokens, state: String): Boolean =
    oauthService.verifyState(oauthTokens, state)

  def findCandidat(peConnectId: PEConnectId): Future[Option[CandidatPEConnect]] =
    peConnectSqlAdapter.findCandidat(peConnectId)

  def getCandidat(candidatId: CandidatId): Future[CandidatPEConnect] =
    peConnectSqlAdapter.getCandidat(candidatId)

  def findCandidatId(peConnectId: PEConnectId): Future[Option[CandidatId]] =
    peConnectSqlAdapter.findCandidatId(peConnectId)

  def saveCandidat(candidatPEConnect: CandidatPEConnect): Future[Unit] =
    peConnectSqlAdapter.saveCandidat(candidatPEConnect)

  def findRecruteur(peConnectId: PEConnectId): Future[Option[RecruteurPEConnect]] =
    peConnectSqlAdapter.findRecruteur(peConnectId)

  def saveRecruteur(recruteurPEConnect: RecruteurPEConnect): Future[Unit] =
    peConnectSqlAdapter.saveRecruteur(recruteurPEConnect)

  def getInfosRecruteur(accessToken: AccessToken): Future[PEConnectRecruteurInfos] =
    peConnectWSAdapter.getInfosRecruteur(accessToken)

  def getInfosCandidat(accessToken: AccessToken): Future[PEConnectCandidatInfos] =
    peConnectWSAdapter.getInfosCandidat(accessToken)

  def getPrestationsCandidat(accessToken: AccessToken): Future[List[MRSValidee]] =
    peConnectWSAdapter.getPrestationsCandidat(accessToken)

  def getAdresseCandidat(accessToken: AccessToken): Future[Adresse] =
    peConnectWSAdapter.getCoordonneesCandidat(accessToken)

  def getStatutDemandeurEmploiCandidat(accessToken: AccessToken): Future[StatutDemandeurEmploi] =
    peConnectWSAdapter.getStatutDemandeurEmploiCandidat(accessToken)

  def getAccessTokenCandidat(authorizationCode: String,
                             redirectUri: String,
                             oauthTokens: OauthTokens): Future[AccessTokenResponse] =
    for {
      accessTokenResponse <- peConnectWSAdapter.getAccessTokenCandidat(authorizationCode = authorizationCode, redirectUri = redirectUri)
      _ <- Either.cond(oauthService.verifyNonce(oauthTokens, accessTokenResponse.nonce), (), "La comparaison du nonce a échoué").toFuture
      _ <- peConnectJWTAdapter.validateCandidatToken(accessTokenResponse.idToken, oauthTokens.nonce)
    } yield accessTokenResponse

  def getAccessTokenRecruteur(authorizationCode: String,
                              redirectUri: String,
                              oauthTokens: OauthTokens): Future[AccessTokenResponse] =
    for {
      accessTokenResponse <- peConnectWSAdapter.getAccessTokenRecruteur(authorizationCode = authorizationCode, redirectUri = redirectUri)
      _ <- Either.cond(oauthService.verifyNonce(oauthTokens, accessTokenResponse.nonce), (), "La comparaison du nonce a échoué").toFuture
      _ <- peConnectJWTAdapter.validateRecruteurToken(accessTokenResponse.idToken, oauthTokens.nonce)
    } yield accessTokenResponse
}
