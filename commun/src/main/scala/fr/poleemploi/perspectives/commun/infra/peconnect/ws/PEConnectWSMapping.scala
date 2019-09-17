package fr.poleemploi.perspectives.commun.infra.peconnect.ws

import java.time.{LocalDateTime, ZonedDateTime}

import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, __}

class PEConnectWSMapping {

  def buildMRSValidees(response: List[ResultatRendezVousResponse]): List[MRSValidee] =
    response.flatMap(resultat =>
      for {
        codeSitePESuiviResultat <- resultat.codeSitePESuiviResultat
        _ <- resultat.listeCodeResultat.filter(_.exists(c => CodeResultatRendezVousResponse.VALIDE == c || CodeResultatRendezVousResponse.VALIDE_EMBAUCHE == c || CodeResultatRendezVousResponse.VALIDE_ENTREE_EN_FORMATION == c))
      } yield MRSValidee(
        codeROME = CodeROME(resultat.codeRome),
        codeDepartement = CodeDepartement(codeSitePESuiviResultat.take(2)),
        dateEvaluation = resultat.dateDebutSession.toLocalDate,
        isDHAE = false
      )
    ).foldLeft(List[MRSValidee]())((acc, mrsValidee) =>
      if (acc.exists(m => m.codeROME == mrsValidee.codeROME && m.codeDepartement == mrsValidee.codeDepartement))
        acc
      else
        mrsValidee :: acc
    )

  def buildPEConnectCandidatInfos(response: UserInfosResponse): PEConnectCandidatInfos =
    PEConnectCandidatInfos(
      peConnectId = PEConnectId(response.sub),
      nom = Nom(response.familyName),
      prenom = Prenom(response.givenName),
      email = response.email.map(e => Email(e.toLowerCase)),
      genre = buildGenre(response.gender)
    )

  def buildPEConnectRecruteurInfos(response: UserInfosEntrepriseResponse): PEConnectRecruteurInfos =
    PEConnectRecruteurInfos(
      peConnectId = PEConnectId(response.sub),
      nom = Nom(response.familyName),
      prenom = Prenom(response.givenName),
      email = Email(response.email.toLowerCase),
      genre = buildGenre(response.gender),
      certifie = buildCertifie(response.habilitation)
    )

  def buildPEConnectRecruteurInfosAlternative(response: UserInfosEntrepriseAlternativeResponse): PEConnectRecruteurInfos =
    PEConnectRecruteurInfos(
      peConnectId = PEConnectId(response.sub),
      nom = Nom(response.familyName),
      prenom = Prenom(response.givenName),
      email = Email(response.email.toLowerCase),
      genre = buildGenre(response.gender),
      certifie = buildCertifieFromArray(response.habilitation)
    )

  def buildStatutDemandeurEmploi(response: StatutCandidatReponse): StatutDemandeurEmploi =
    response.codeStatutIndividu match {
      case "0" => StatutDemandeurEmploi.NON_DEMANDEUR_EMPLOI
      case "1" => StatutDemandeurEmploi.DEMANDEUR_EMPLOI
      case code@_ => throw new IllegalArgumentException(s"CodeStatutIndividu non géré : $code")
    }

  def buildAdresse(response: CoordonneesCandidatReponse): Option[Adresse] =
    for {
      voie <- response.adresse4
      codePostal <- response.codePostal
      libelleCommune <- response.libelleCommune
      libellePays <- response.libellePays
    } yield
      Adresse(
        voie = voie.toLowerCase,
        codePostal = codePostal,
        libelleCommune = libelleCommune.toLowerCase.capitalize,
        libellePays = libellePays.toLowerCase.capitalize
      )

  def buildSavoirEtreProfessionnels(responses: List[CompetenceResponse]): List[SavoirEtre] =
    responses
      .filter(_.typeCompetence == TypeCompetenceResponse.SAVOIR_ETRE)
      .flatMap(c => c.libelle.map(SavoirEtre(_)))

  def buildSavoirFaire(responses: List[CompetenceResponse]): List[SavoirFaire] =
    responses
      .filter(_.typeCompetence == TypeCompetenceResponse.SAVOIR_FAIRE_METIER)
      .flatMap(c => c.libelle.map(l => SavoirFaire(
        label = l,
        niveau = c.niveau.flatMap(buildNiveauSavoirFaire)
      )))

  def buildLanguesCandidat(responses: List[LangueResponse]): List[Langue] =
    responses.map(l => Langue(
      label = l.libelle,
      niveau = l.niveau.flatMap(buildNiveauLangue)
    ))

  def buildCentreInteretsCandidat(responses: List[CentreInteretResponse]): List[CentreInteret] =
    responses.map(c => CentreInteret(c.intitule))

  def buildPermis(responses: List[PermisResponse]): List[Permis] =
    responses.flatMap(p => p.code.map(c =>
      Permis(
        code = c,
        label = p.libelle.replaceFirst(s"$c - ", "")
      )
    ))

  def buildFormations(responses: List[FormationResponse]): List[Formation] =
    for {
      f <- responses
      anneeFin <- f.anneeFin
      intitule <- f.intitule
    } yield {
      Formation(
        anneeFin = anneeFin,
        intitule = intitule,
        lieu = f.lieu,
        domaine = f.domaine.map(d => DomaineFormation(d.libelle)),
        niveau = f.niveau.map(n => NiveauFormation(n.libelle))
      )
    }

  def buildExperienceProfessionnelles(responses: List[ExperienceProfessionnelleResponse]): List[ExperienceProfessionnelle] =
    for {
      e <- responses
      intitule <- e.intitule
      dateDebut <- e.date.flatMap(_.debut)
    } yield {
      ExperienceProfessionnelle(
        intitule = intitule,
        dateDebut = dateDebut.toLocalDate,
        dateFin = e.date.flatMap(_.fin).map(_.toLocalDate),
        enPoste = e.enPoste,
        nomEntreprise = e.entreprise,
        lieu = e.lieu,
        description = e.description
      )
    }

  private def buildGenre(gender: String): Genre = gender match {
    case "male" => Genre.HOMME
    case "female" => Genre.FEMME
    case g@_ => throw new IllegalArgumentException(s"Gender non géré : $g")
  }

  private def buildCertifie(habilitation: Option[String]): Boolean =
    buildCertifieFromArray(habilitation.map(Seq(_)))

  private def buildCertifieFromArray(habilitation: Option[Seq[String]]): Boolean = habilitation match {
    case Some(x) if x.contains("recruteurcertifie") => true
    case _ => false
  }

  private def buildNiveauLangue(niveauLangueResponse: NiveauLangueResponse): Option[NiveauLangue] = niveauLangueResponse.code match {
    case "1" => Some(NiveauLangue.DEBUTANT)
    case "2" => Some(NiveauLangue.INTERMEDIAIRE)
    case "3" => Some(NiveauLangue.COURANT)
    case _ => None
  }

  private def buildNiveauSavoirFaire(niveauCompetenceResponse: NiveauCompetenceResponse): Option[NiveauSavoirFaire] = niveauCompetenceResponse.code match {
    case "1" => Some(NiveauSavoirFaire.DEBUTANT)
    case "2" => Some(NiveauSavoirFaire.INTERMEDIAIRE)
    case "3" => Some(NiveauSavoirFaire.AVANCE)
    case _ => None
  }
}

case class PEConnectCandidatInfos(peConnectId: PEConnectId,
                                  nom: Nom,
                                  prenom: Prenom,
                                  email: Option[Email],
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
                                         email: Option[String],
                                         gender: String)

object UserInfosResponse {

  implicit val reads: Reads[UserInfosResponse] = (
    (JsPath \ "sub").read[String] and
      (JsPath \ "family_name").read[String] and
      (JsPath \ "given_name").read[String] and
      (JsPath \ "email").readNullable[String] and
      (JsPath \ "gender").read[String]
    ) (UserInfosResponse.apply _)
}

private[ws] case class UserInfosEntrepriseResponse(sub: String,
                                                   familyName: String,
                                                   givenName: String,
                                                   email: String,
                                                   gender: String,
                                                   habilitation: Option[String])

object UserInfosEntrepriseResponse {

  implicit val reads: Reads[UserInfosEntrepriseResponse] = (
    (JsPath \ "sub").read[String] and
      (JsPath \ "family_name").read[String] and
      (JsPath \ "given_name").read[String] and
      (JsPath \ "email").read[String] and
      (JsPath \ "gender").read[String] and
      (JsPath \ "habilitation").readNullable[String]
    ) (UserInfosEntrepriseResponse.apply _)
}

private[ws] case class UserInfosEntrepriseAlternativeResponse(sub: String,
                                                              familyName: String,
                                                              givenName: String,
                                                              email: String,
                                                              gender: String,
                                                              habilitation: Option[Seq[String]])

object UserInfosEntrepriseAlternativeResponse {

  implicit val reads: Reads[UserInfosEntrepriseAlternativeResponse] = (
    (JsPath \ "sub").read[String] and
      (JsPath \ "family_name").read[String] and
      (JsPath \ "given_name").read[String] and
      (JsPath \ "email").read[String] and
      (JsPath \ "gender").read[String] and
      (JsPath \ "habilitation").readNullable[Seq[String]]
    ) (UserInfosEntrepriseAlternativeResponse.apply _)
}

private[ws] case class CoordonneesCandidatReponse(adresse1: Option[String],
                                                  adresse2: Option[String],
                                                  adresse3: Option[String],
                                                  adresse4: Option[String],
                                                  codePostal: Option[String],
                                                  codeINSEE: Option[String],
                                                  libelleCommune: Option[String],
                                                  codePays: Option[String],
                                                  libellePays: Option[String])

private[ws] object CoordonneesCandidatReponse {

  implicit val reads: Reads[CoordonneesCandidatReponse] = Json.reads[CoordonneesCandidatReponse]
}

private[ws] case class StatutCandidatReponse(codeStatutIndividu: String,
                                             libelleStatutIndividu: String)

private[ws] object StatutCandidatReponse {

  implicit val reads: Reads[StatutCandidatReponse] = Json.reads[StatutCandidatReponse]
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

private[ws] case class TypeCompetenceResponse(value: String)

object TypeCompetenceResponse {

  val SAVOIR_FAIRE_METIER = TypeCompetenceResponse(value = "S")
  val SAISIE_LIBRE = TypeCompetenceResponse(value = "L")
  val SAVOIR_ETRE = TypeCompetenceResponse(value = "Q")

  implicit val reads: Reads[TypeCompetenceResponse] =
    __.readNullable[String].map(_.map(TypeCompetenceResponse(_)).getOrElse(TypeCompetenceResponse("")))
}

private[ws] case class NiveauCompetenceResponse(code: String,
                                                libelle: String)

private[ws] object NiveauCompetenceResponse {

  implicit val reads: Reads[NiveauCompetenceResponse] = Json.reads[NiveauCompetenceResponse]
}

private[ws] case class CompetenceResponse(libelle: Option[String],
                                          niveau: Option[NiveauCompetenceResponse],
                                          typeCompetence: TypeCompetenceResponse)

private[ws] object CompetenceResponse {

  implicit val reads: Reads[CompetenceResponse] = (
    (JsPath \ "libelle").readNullable[String] and
      (JsPath \ "niveau").readNullable[NiveauCompetenceResponse] and
      (JsPath \ "type").read[TypeCompetenceResponse]
    ) (CompetenceResponse.apply _)
}

private[ws] case class CentreInteretResponse(complement: Option[String],
                                             intitule: String)

private[ws] object CentreInteretResponse {

  implicit val reads: Reads[CentreInteretResponse] = Json.reads[CentreInteretResponse]
}

private[ws] case class NiveauLangueResponse(code: String,
                                            libelle: String)

private[ws] object NiveauLangueResponse {

  implicit val reads: Reads[NiveauLangueResponse] = Json.reads[NiveauLangueResponse]
}

private[ws] case class LangueResponse(code: Option[String],
                                      libelle: String,
                                      niveau: Option[NiveauLangueResponse])

private[ws] object LangueResponse {

  implicit val reads: Reads[LangueResponse] = Json.reads[LangueResponse]
}

private[ws] case class PermisResponse(code: Option[String],
                                      libelle: String)

private[ws] object PermisResponse {

  implicit val reads: Reads[PermisResponse] = Json.reads[PermisResponse]
}

private[ws] case class DomaineFormationResponse(code: String,
                                                libelle: String)

private[ws] object DomaineFormationResponse {

  implicit val reads: Reads[DomaineFormationResponse] = Json.reads[DomaineFormationResponse]
}

private[ws] case class NiveauFormationResponse(code: String,
                                               libelle: String)

private[ws] object NiveauFormationResponse {

  implicit val reads: Reads[NiveauFormationResponse] = Json.reads[NiveauFormationResponse]
}

private[ws] case class FormationResponse(anneeFin: Option[Int],
                                         description: Option[String],
                                         diplomeObtenu: Boolean,
                                         etranger: Boolean,
                                         intitule: Option[String],
                                         lieu: Option[String],
                                         niveau: Option[NiveauFormationResponse],
                                         domaine: Option[DomaineFormationResponse])

private[ws] object FormationResponse {

  implicit val reads: Reads[FormationResponse] = Json.reads[FormationResponse]
}

private[ws] case class DateExperienceProfessionnelleResponse(debut: Option[LocalDateTime],
                                                             fin: Option[LocalDateTime])

private[ws] object DateExperienceProfessionnelleResponse {

  implicit val reads: Reads[DateExperienceProfessionnelleResponse] = Json.reads[DateExperienceProfessionnelleResponse]
}

private[ws] case class ExperienceProfessionnelleResponse(date: Option[DateExperienceProfessionnelleResponse],
                                                         description: Option[String],
                                                         duree: Option[Long],
                                                         enPoste: Boolean,
                                                         entreprise: Option[String],
                                                         etranger: Boolean,
                                                         intitule: Option[String],
                                                         lieu: Option[String])

private[ws] object ExperienceProfessionnelleResponse {

  implicit val reads: Reads[ExperienceProfessionnelleResponse] = Json.reads[ExperienceProfessionnelleResponse]

}
