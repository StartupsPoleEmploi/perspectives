package fr.poleemploi.perspectives.authentification.infra.peconnect.ws

import java.util.concurrent.TimeUnit

import fr.poleemploi.perspectives.authentification.infra.peconnect.jwt.JWTToken
import fr.poleemploi.perspectives.commun.infra.ws.AccessToken
import play.api.libs.json.{JsPath, Reads}
import play.api.libs.functional.syntax._

import scala.concurrent.duration.Duration

case class AccessTokenResponse(accessToken: AccessToken,
                               idToken: JWTToken,
                               nonce: String,
                               expiresIn: Duration)

object AccessTokenResponse {

  implicit val reads: Reads[AccessTokenResponse] = (
    (JsPath \ "access_token").read[String].map(AccessToken) and
      (JsPath \ "id_token").read[String].map(JWTToken) and
      (JsPath \ "nonce").read[String] and
      (JsPath \ "expires_in").read[Long].map(l => Duration(length = l, unit = TimeUnit.SECONDS))
    ) (AccessTokenResponse.apply _)
}
