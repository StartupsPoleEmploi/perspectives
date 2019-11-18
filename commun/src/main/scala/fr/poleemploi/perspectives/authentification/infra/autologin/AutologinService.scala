package fr.poleemploi.perspectives.authentification.infra.autologin

import java.time.Clock
import java.util.UUID

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{Email, Nom, Prenom}
import fr.poleemploi.perspectives.recruteur.RecruteurId
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson}
import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}

import scala.util.{Failure, Success, Try}

class AutologinService(autologinConfig: AutologinConfig) {

  import AutologinService._
  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val clock: Clock = Clock.systemUTC

  def genererTokenCandidat(candidatId: CandidatId,
                           nom: Nom,
                           prenom: Prenom,
                           email: Email): JwtToken =
    genererToken(candidatId.value, nom, prenom, email)

  def genererTokenRecruteur(recruteurId: RecruteurId,
                            nom: Nom,
                            prenom: Prenom,
                            email: Email): JwtToken =
    genererToken(recruteurId.value, nom, prenom, email, isCandidat = false)

  def extractAutologinToken(token: String): Try[AutologinToken] =
    JwtJson.decodeJson(token = token, key = autologinConfig.secretKey, algorithms = Seq(ALGORITHM)).flatMap { decodedClaim =>
      decodedClaim.validate[AutologinToken] match {
        case JsSuccess(autologinToken, _) => Success(autologinToken)
        case e: JsError => Failure(new IllegalArgumentException(s"Error occured when trying to deserialize ${Json.stringify(decodedClaim)} to AutologinToken : ${JsError.toJson(e)}"))
      }
    }

  private def genererToken(identifiant: String,
                           nom: Nom,
                           prenom: Prenom,
                           email: Email,
                           isCandidat: Boolean = true): JwtToken = {
    import pdi.jwt.JwtJson._

    val autologinToken = AutologinToken(
      identifiant = identifiant,
      nom = nom,
      prenom = prenom,
      email = Some(email),
      typeUtilisateur = if (isCandidat) TypeUtilisateur.CANDIDAT else TypeUtilisateur.RECRUTEUR
    )
    val jwtClaim = JwtClaim()
      .withId(UUID.randomUUID().toString)
      .by(autologinConfig.issuer)
      .about(identifiant)
      .issuedNow
      .expiresIn(autologinConfig.expirationInSeconds)

    val jwtClaimAsJson = Json.toJson(jwtClaim).as[JsObject] ++ Json.toJson(autologinToken).as[JsObject]
    JwtToken(JwtJson.encode(claim = jwtClaimAsJson, key = autologinConfig.secretKey, algorithm = ALGORITHM))
  }
}

object AutologinService {

  val ALGORITHM: JwtAlgorithm.HS256.type = JwtAlgorithm.HS256
}
