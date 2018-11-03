package fr.poleemploi.perspectives.authentification.infra.peconnect.ws

import fr.poleemploi.perspectives.candidat.{Adresse, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.domain.{Email, Genre}
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

private[ws] object PEConnectWSMapping {

  def extractGender(gender: String): Genre = gender match {
    case "male" => Genre.HOMME
    case "female" => Genre.FEMME
    case g@_ => throw new IllegalArgumentException(s"Gender non géré : $g")
  }

  def extractCertifie(habilitation: String): Boolean = habilitation match {
    case "recruteurcertifie" => true
    case _ => false
  }
}

case class AccessToken(value: String)

case class JWTToken(value: String)

case class AccessTokenResponse(accessToken: AccessToken,
                               idToken: JWTToken,
                               nonce: String)

object AccessTokenResponse {

  implicit val accessTokenResponseReads: Reads[AccessTokenResponse] = (
    (JsPath \ "access_token").read[String].map(AccessToken) and
      (JsPath \ "id_token").read[String].map(JWTToken) and
      (JsPath \ "nonce").read[String]
    ) (AccessTokenResponse.apply _)
}

case class PEConnectCandidatInfos(peConnectId: PEConnectId,
                                  nom: String,
                                  prenom: String,
                                  email: Email,
                                  genre: Genre)

case class PEConnectRecruteurInfos(peConnectId: PEConnectId,
                                   nom: String,
                                   prenom: String,
                                   email: Email,
                                   genre: Genre,
                                   certifie: Boolean)

private[ws] case class UserInfosResponse(sub: String,
                                         familyName: String,
                                         givenName: String,
                                         email: String,
                                         gender: String) {

  def toPEConnectCandidatInfos: PEConnectCandidatInfos =
    PEConnectCandidatInfos(
      peConnectId = PEConnectId(sub),
      nom = familyName.toLowerCase,
      prenom = givenName.toLowerCase,
      email = Email(email.toLowerCase), // on fait confiance à PEConnect pour avoir un email valide
      genre = PEConnectWSMapping.extractGender(gender)
    )
}

object UserInfosResponse {

  implicit val userInfoResponseReads: Reads[UserInfosResponse] = (
    (JsPath \ "sub").read[String] and
      (JsPath \ "family_name").read[String] and
      (JsPath \ "given_name").read[String] and
      (JsPath \ "email").read[String] and
      (JsPath \ "gender").read[String]
    ) (UserInfosResponse.apply _)
}

private[ws] case class UserInfosEntrepriseResponse(sub: String,
                                                   familyName: String,
                                                   givenName: String,
                                                   email: String,
                                                   gender: String,
                                                   habilitation: String) {

  def toPEConnectRecruteurInfos: PEConnectRecruteurInfos =
    PEConnectRecruteurInfos(
      peConnectId = PEConnectId(sub),
      nom = familyName.toLowerCase,
      prenom = givenName.toLowerCase,
      email = Email(email.toLowerCase), // on fait confiance à PEConnect pour avoir un email valide
      genre = PEConnectWSMapping.extractGender(gender),
      certifie = PEConnectWSMapping.extractCertifie(habilitation)
    )
}

object UserInfosEntrepriseResponse {

  implicit val jsonReads: Reads[UserInfosEntrepriseResponse] = (
    (JsPath \ "sub").read[String] and
      (JsPath \ "family_name").read[String] and
      (JsPath \ "given_name").read[String] and
      (JsPath \ "email").read[String] and
      (JsPath \ "gender").read[String] and
      (JsPath \ "habilitation").read[String]
    ) (UserInfosEntrepriseResponse.apply _)
}

private[ws] case class CoordonneesCandidatReponse(adresse1: Option[String],
                                                  adresse2: Option[String],
                                                  adresse3: Option[String],
                                                  adresse4: String,
                                                  codePostal: String,
                                                  codeINSEE: String,
                                                  libelleCommune: String,
                                                  codePays: String,
                                                  libellePays: String) {

  def toAdresse: Adresse =
    Adresse(
      voie = adresse4.toLowerCase,
      codePostal = codePostal,
      libelleCommune = libelleCommune.toLowerCase.capitalize,
      libellePays = libellePays.toLowerCase.capitalize
    )
}

private[ws] object CoordonneesCandidatReponse {

  implicit val coordoneesCandidatReads: Reads[CoordonneesCandidatReponse] = (
    (JsPath \ "adresse1").readNullable[String] and
      (JsPath \ "adresse2").readNullable[String] and
      (JsPath \ "adresse3").readNullable[String] and
      (JsPath \ "adresse4").read[String] and
      (JsPath \ "codePostal").read[String] and
      (JsPath \ "codeINSEE").read[String] and
      (JsPath \ "libelleCommune").read[String] and
      (JsPath \ "codePays").read[String] and
      (JsPath \ "libellePays").read[String]
    ) (CoordonneesCandidatReponse.apply _)
}

private[ws] case class StatutCandidatReponse(codeStatutIndividu: String,
                                             libelleStatutIndividu: String) {

  def toStatutDemandeurEmploi: StatutDemandeurEmploi =
    codeStatutIndividu match {
      case "0" => StatutDemandeurEmploi.NON_DEMANDEUR_EMPLOI
      case "1" => StatutDemandeurEmploi.DEMANDEUR_EMPLOI
      case code@_ => throw new IllegalArgumentException(s"CodeStatutIndividu non géré : $code")
    }
}

private[ws] object StatutCandidatReponse {

  implicit val statutCandidatReponseReads: Reads[StatutCandidatReponse] = (
    (JsPath \ "codeStatutIndividu").read[String] and
      (JsPath \ "libelleStatutIndividu").read[String]
    ) (StatutCandidatReponse.apply _)
}