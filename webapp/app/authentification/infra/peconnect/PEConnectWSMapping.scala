package authentification.infra.peconnect

import fr.poleemploi.perspectives.domain.Genre
import fr.poleemploi.perspectives.domain.authentification.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.domain.candidat.Adresse
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class AccessTokenResponse(accessToken: String,
                               idToken: String,
                               nonce: String)

object AccessTokenResponse {

  implicit val accessTokenResponseReads: Reads[AccessTokenResponse] = (
    (JsPath \ "access_token").read[String] and
      (JsPath \ "id_token").read[String] and
      (JsPath \ "nonce").read[String]
    ) (AccessTokenResponse.apply _)
}

case class PEConnectCandidatInfos(peConnectId: PEConnectId,
                                  nom: String,
                                  prenom: String,
                                  email: String,
                                  genre: Genre)

private[peconnect] case class CandidatUserInfos(sub: String,
                                                familyName: String,
                                                givenName: String,
                                                email: String,
                                                gender: String)

object CandidatUserInfos {

  implicit val candidatUserInfosReads: Reads[CandidatUserInfos] = (
    (JsPath \ "sub").read[String] and
      (JsPath \ "family_name").read[String] and
      (JsPath \ "given_name").read[String] and
      (JsPath \ "email").read[String] and
      (JsPath \ "gender").read[String]
    ) (CandidatUserInfos.apply _)

  def toPEConnectCandidatInfos(candidatUserInfos: CandidatUserInfos): PEConnectCandidatInfos =
    PEConnectCandidatInfos(
      peConnectId = PEConnectId(candidatUserInfos.sub),
      nom = candidatUserInfos.familyName.toLowerCase,
      prenom = candidatUserInfos.givenName.toLowerCase,
      email = candidatUserInfos.email.toLowerCase,
      genre = Gender.extractGender(candidatUserInfos.gender)
    )
}

case class PEConnectRecruteurInfos(peConnectId: PEConnectId,
                                   nom: String,
                                   prenom: String,
                                   email: String,
                                   genre: Genre)

private[peconnect] case class RecruteurUserInfos(sub: String,
                                                 familyName: String,
                                                 givenName: String,
                                                 email: String,
                                                 gender: String)

object RecruteurUserInfos {

  implicit val recruteurUserInfosReads: Reads[RecruteurUserInfos] = (
    (JsPath \ "sub").read[String] and
      (JsPath \ "family_name").read[String] and
      (JsPath \ "given_name").read[String] and
      (JsPath \ "email").read[String] and
      (JsPath \ "gender").read[String]
    ) (RecruteurUserInfos.apply _)

  def toPEConnectRecruteurInfos(recruteurUserInfos: RecruteurUserInfos): PEConnectRecruteurInfos =
    PEConnectRecruteurInfos(
      peConnectId = PEConnectId(recruteurUserInfos.sub),
      nom = recruteurUserInfos.familyName.toLowerCase,
      prenom = recruteurUserInfos.givenName.toLowerCase,
      email = recruteurUserInfos.email.toLowerCase,
      genre = Gender.extractGender(recruteurUserInfos.gender)
    )
}

private[peconnect] object Gender {

  def extractGender(gender: String): Genre = gender match {
    case "male" => Genre.HOMME
    case "female" => Genre.FEMME
    case g@_ => throw new IllegalArgumentException(s"Genre inconnu : $g")
  }
}

private[peconnect] case class CoordonneesCandidatResponse(adresse1: Option[String],
                                                           adresse2: Option[String],
                                                           adresse3: Option[String],
                                                           adresse4: String,
                                                           codePostal: String,
                                                           codeINSEE: String,
                                                           libelleCommune: String,
                                                           codePays: String,
                                                           libellePays: String)

private[peconnect] object CoordonneesCandidatResponse {

  implicit val coordoneesCandidatReads: Reads[CoordonneesCandidatResponse] = (
    (JsPath \ "adresse1").readNullable[String] and
      (JsPath \ "adresse2").readNullable[String] and
      (JsPath \ "adresse3").readNullable[String] and
      (JsPath \ "adresse4").read[String] and
      (JsPath \ "codePostal").read[String] and
      (JsPath \ "codeINSEE").read[String] and
      (JsPath \ "libelleCommune").read[String] and
      (JsPath \ "codePays").read[String] and
      (JsPath \ "libellePays").read[String]
    ) (CoordonneesCandidatResponse.apply _)

  def buildAdresse(coordonneesCandidatResponse: CoordonneesCandidatResponse): Adresse =
    Adresse(
      voie = coordonneesCandidatResponse.adresse4.toLowerCase,
      codePostal = coordonneesCandidatResponse.codePostal,
      libelleCommune = coordonneesCandidatResponse.libelleCommune.toLowerCase.capitalize,
      libellePays = coordonneesCandidatResponse.libellePays.toLowerCase.capitalize
    )
}