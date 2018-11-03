package fr.poleemploi.perspectives.authentification.infra.peconnect.jwt

import play.api.libs.json.{Format, Json}

case class JWTClaims(iss: String,
                     azp: Option[String],
                     realm: String,
                     nonce: String)

object JWTClaims {
  implicit val jsonFormat: Format[JWTClaims] = Json.format[JWTClaims]
}
