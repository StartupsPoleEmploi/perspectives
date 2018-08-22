package fr.poleemploi.perspectives.domain.metier.infra

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class AccessTokenResponse(accessToken: String,
                               tokenType: String,
                               scope: String,
                               expiresIn: Int)

object AccessTokenResponse {

  implicit val accessTokenResponseReads: Reads[AccessTokenResponse] = (
    (JsPath \ "access_token").read[String] and
      (JsPath \ "token_type").read[String] and
      (JsPath \ "scope").read[String] and
      (JsPath \ "expires_in").read[Int]
    ) (AccessTokenResponse.apply _)
}
