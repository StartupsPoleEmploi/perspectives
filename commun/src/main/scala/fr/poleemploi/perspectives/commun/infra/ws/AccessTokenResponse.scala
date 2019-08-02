package fr.poleemploi.perspectives.commun.infra.ws

import java.util.concurrent.TimeUnit

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

import scala.concurrent.duration.Duration

case class AccessTokenResponse(accessToken: AccessToken,
                               tokenType: String,
                               scope: String,
                               expiresIn: Duration)

object AccessTokenResponse {

  implicit val reads: Reads[AccessTokenResponse] = (
    (JsPath \ "access_token").read[String].map(AccessToken) and
      (JsPath \ "token_type").read[String] and
      (JsPath \ "scope").read[String] and
      (JsPath \ "expires_in").read[Long].map(l => Duration(length = l, unit = TimeUnit.SECONDS))
    ) (AccessTokenResponse.apply _)
}