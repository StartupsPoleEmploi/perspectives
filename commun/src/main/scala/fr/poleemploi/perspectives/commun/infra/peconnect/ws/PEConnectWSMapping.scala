package fr.poleemploi.perspectives.commun.infra.peconnect.ws

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.candidat.{Adresse, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, __}

class PEConnectWSMapping {

  def buildMRSValidee(response: ResultatRendezVousResponse): Option[MRSValidee] =
    for {
      codeSitePESuiviResultat <- response.codeSitePESuiviResultat
      _ <- response.listeCodeResultat.filter(_.exists(c => CodeResultatRendezVousResponse.VALIDE == c || CodeResultatRendezVousResponse.VALIDE_EMBAUCHE == c || CodeResultatRendezVousResponse.VALIDE_ENTREE_EN_FORMATION == c))
    } yield MRSValidee(
      codeROME = CodeROME(response.codeRome),
      codeDepartement = CodeDepartement(codeSitePESuiviResultat.take(2)),
      dateEvaluation = response.dateDebutSession.toLocalDate
    )

  def buildPEConnectCandidatInfos(response: UserInfosResponse): PEConnectCandidatInfos =
    PEConnectCandidatInfos(
      peConnectId = PEConnectId(response.sub),
      nom = Nom(response.familyName),
      prenom = Prenom(response.givenName),
      email = Email(response.email.toLowerCase),
      genre = buildGender(response.gender)
    )

  def buildPEConnectRecruteurInfos(response: UserInfosEntrepriseResponse): PEConnectRecruteurInfos =
    PEConnectRecruteurInfos(
      peConnectId = PEConnectId(response.sub),
      nom = Nom(response.familyName),
      prenom = Prenom(response.givenName),
      email = Email(response.email.toLowerCase),
      genre = buildGender(response.gender),
      certifie = buildCertifie(response.habilitation)
    )

  def buildStatutDemandeurEmploi(response: StatutCandidatReponse): StatutDemandeurEmploi =
    response.codeStatutIndividu match {
      case "0" => StatutDemandeurEmploi.NON_DEMANDEUR_EMPLOI
      case "1" => StatutDemandeurEmploi.DEMANDEUR_EMPLOI
      case code@_ => throw new IllegalArgumentException(s"CodeStatutIndividu non géré : $code")
    }

  def buildAdresse(response: CoordonneesCandidatReponse): Adresse =
    Adresse(
      voie = response.adresse4.toLowerCase,
      codePostal = response.codePostal,
      libelleCommune = response.libelleCommune.toLowerCase.capitalize,
      libellePays = response.libellePays.toLowerCase.capitalize
    )

  private def buildGender(gender: String): Genre = gender match {
    case "male" => Genre.HOMME
    case "female" => Genre.FEMME
    case g@_ => throw new IllegalArgumentException(s"Gender non géré : $g")
  }

  private def buildCertifie(habilitation: String): Boolean = habilitation match {
    case "recruteurcertifie" => true
    case _ => false
  }
}

case class PEConnectCandidatInfos(peConnectId: PEConnectId,
                                  nom: Nom,
                                  prenom: Prenom,
                                  email: Email,
                                  genre: Genre)

case class PEConnectRecruteurInfos(peConnectId: PEConnectId,
                                   nom: Nom,
                                   prenom: Prenom,
                                   email: Email,
                                   genre: Genre,
                                   certifie: Boolean)

private[ws] case class UserInfosResponse(sub: String,
                                         familyName: String,
                                         givenName: String,
                                         email: String,
                                         gender: String)

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
                                                   habilitation: String)

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
                                                  libellePays: String)

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
                                             libelleStatutIndividu: String)

private[ws] object StatutCandidatReponse {

  implicit val statutCandidatReponseReads: Reads[StatutCandidatReponse] = (
    (JsPath \ "codeStatutIndividu").read[String] and
      (JsPath \ "libelleStatutIndividu").read[String]
    ) (StatutCandidatReponse.apply _)
}

private[ws] case class CodeResultatRendezVousResponse(value: String)

object CodeResultatRendezVousResponse {

  val VALIDE = CodeResultatRendezVousResponse(value = "VSL")
  val VALIDE_EMBAUCHE = CodeResultatRendezVousResponse(value = "VEM")
  val VALIDE_ENTREE_EN_FORMATION = CodeResultatRendezVousResponse(value = "VEF")

  implicit val reads: Reads[CodeResultatRendezVousResponse] =
    __.readNullable[String].map(_.map(CodeResultatRendezVousResponse(_)).getOrElse(CodeResultatRendezVousResponse("")))
}

private[ws] case class ResultatRendezVousResponse(codeRome: String,
                                                  dateDebutSession: ZonedDateTime,
                                                  codeSitePESuiviResultat: Option[String],
                                                  listeCodeResultat: Option[List[CodeResultatRendezVousResponse]])

private[ws] object ResultatRendezVousResponse {

  implicit val reads: Reads[ResultatRendezVousResponse] = Json.reads[ResultatRendezVousResponse]
}