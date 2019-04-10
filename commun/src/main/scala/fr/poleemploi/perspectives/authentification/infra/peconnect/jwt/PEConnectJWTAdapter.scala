package fr.poleemploi.perspectives.authentification.infra.peconnect.jwt

import java.util.Base64

import fr.poleemploi.perspectives.commun.infra.oauth.OauthConfig
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PEConnectJWTAdapter(recruteurOauthConfig: OauthConfig,
                          candidatOauthConfig: OauthConfig) {

  def validateCandidatToken(jwtToken: JWTToken, nonce: String): Future[Unit] =
    validateToken(
      jwtToken = jwtToken,
      nonce = nonce,
      oauthConfig = candidatOauthConfig
    )

  def validateRecruteurToken(jwtToken: JWTToken, nonce: String): Future[Unit] =
    validateToken(
      jwtToken = jwtToken,
      nonce = nonce,
      oauthConfig = recruteurOauthConfig
    )

  private def validateToken(jwtToken: JWTToken, nonce: String, oauthConfig: OauthConfig): Future[Unit] = Future {
    val parts = jwtToken.value.split("\\.")
    val claims = parts(1)
    val jwtClaims = Json.parse(Base64.getUrlDecoder.decode(claims)).as[JWTClaims]

    for {
      _ <- Either.cond(jwtClaims.iss.startsWith(oauthConfig.urlAuthentification), (), "Claim issuer invalide")
      _ <- Either.cond(jwtClaims.realm != s"${oauthConfig.realm}", (), "Claim realm invalide")
      _ <- Either.cond(jwtClaims.azp.exists(_ != oauthConfig.clientId), (), "Claim azp invalide")
      _ <- Either.cond(jwtClaims.nonce != nonce, (), "Claim nonce invalide")
    } yield ()
  }
}
