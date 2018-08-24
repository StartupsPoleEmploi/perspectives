package authentification.infra.peconnect

import fr.poleemploi.perspectives.domain.Genre
import fr.poleemploi.perspectives.domain.authentification.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.domain.candidat.{Adresse, StatutDemandeurEmploi}
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

private[peconnect] object PEConnectWSMapping {

  def extractGender(gender: String): Genre = gender match {
    case "male" => Genre.HOMME
    case "female" => Genre.FEMME
    case g@_ => throw new IllegalArgumentException(s"Gender inconnu : $g")
  }
}

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
                                                gender: String) {

  def toPEConnectCandidatInfos: PEConnectCandidatInfos =
    PEConnectCandidatInfos(
      peConnectId = PEConnectId(sub),
      nom = familyName.toLowerCase,
      prenom = givenName.toLowerCase,
      email = email.toLowerCase,
      genre = PEConnectWSMapping.extractGender(gender)
    )
}

object CandidatUserInfos {

  implicit val candidatUserInfosReads: Reads[CandidatUserInfos] = (
    (JsPath \ "sub").read[String] and
      (JsPath \ "family_name").read[String] and
      (JsPath \ "given_name").read[String] and
      (JsPath \ "email").read[String] and
      (JsPath \ "gender").read[String]
    ) (CandidatUserInfos.apply _)
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
                                                 gender: String) {

  def toPEConnectRecruteurInfos: PEConnectRecruteurInfos =
    PEConnectRecruteurInfos(
      peConnectId = PEConnectId(sub),
      nom = familyName.toLowerCase,
      prenom = givenName.toLowerCase,
      email = email.toLowerCase,
      genre = PEConnectWSMapping.extractGender(gender)
    )
}

object RecruteurUserInfos {

  implicit val recruteurUserInfosReads: Reads[RecruteurUserInfos] = (
    (JsPath \ "sub").read[String] and
      (JsPath \ "family_name").read[String] and
      (JsPath \ "given_name").read[String] and
      (JsPath \ "email").read[String] and
      (JsPath \ "gender").read[String]
    ) (RecruteurUserInfos.apply _)
}

private[peconnect] case class CoordonneesCandidatReponse(adresse1: Option[String],
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

private[peconnect] object CoordonneesCandidatReponse {

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

private[peconnect] case class StatutCandidatReponse(codeStatutIndividu: String,
                                                    libelleStatutIndividu: String) {

  def toStatutDemandeurEmploi: StatutDemandeurEmploi =
    codeStatutIndividu match {
      case "0" => StatutDemandeurEmploi.NON_DEMANDEUR_EMPLOI
      case "1" => StatutDemandeurEmploi.DEMANDEUR_EMPLOI
      case code@_ => throw new IllegalArgumentException(s"CodeStatutIndividu non géré : $code")
    }
}

private[peconnect] object StatutCandidatReponse {

  implicit val statutCandidatReponseReads: Reads[StatutCandidatReponse] = (
    (JsPath \ "codeStatutIndividu").read[String] and
      (JsPath \ "libelleStatutIndividu").read[String]
    ) (StatutCandidatReponse.apply _)
}