package fr.poleemploi.perspectives.offre.infra.ws

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.offre.domain._
import play.api.libs.json.{Json, Reads, __}

case class CommuneResponse(code: String,
                           codePostal: String)

object CommuneResponse {

  implicit val reads: Reads[CommuneResponse] = Json.reads[CommuneResponse]
}

case class ContactResponse(nom: Option[String],
                           courriel: Option[String],
                           telephone: Option[String],
                           urlPostulation: Option[String],
                           coordonnees1: Option[String],
                           coordonnees2: Option[String],
                           coordonnees3: Option[String])

object ContactResponse {

  implicit val reads: Reads[ContactResponse] = Json.reads[ContactResponse]
}

case class CompetenceResponse(libelle: Option[String],
                              code: Option[String])

object CompetenceResponse {

  implicit val reads: Reads[CompetenceResponse] = Json.reads[CompetenceResponse]
}

case class SalaireResponse(libelle: Option[String],
                           commentaire: Option[String])

object SalaireResponse {

  implicit val reads: Reads[SalaireResponse] = Json.reads[SalaireResponse]
}

case class LieuTravailResponse(codePostal: Option[String],
                               libelle: String)

object LieuTravailResponse {

  implicit val reads: Reads[LieuTravailResponse] = Json.reads[LieuTravailResponse]
}

case class EntrepriseResponse(nom: Option[String],
                              description: Option[String])

object EntrepriseResponse {

  implicit val reads: Reads[EntrepriseResponse] = Json.reads[EntrepriseResponse]
}

case class OrigineOffreResponse(origine: Option[String],
                                urlOrigine: String)

object OrigineOffreResponse {

  implicit val reads: Reads[OrigineOffreResponse] = Json.reads[OrigineOffreResponse]
}

case class ExperienceExigeResponse(value: String)

object ExperienceExigeResponse {

  val DEBUTANT_ACCEPTE = ExperienceExigeResponse(value = "D")
  val SOUHAITE = ExperienceExigeResponse(value = "S")
  val EXIGE = ExperienceExigeResponse(value = "E")

  implicit val reads: Reads[ExperienceExigeResponse] =
    (__ \ "experienceExige").read[String].map(ExperienceExigeResponse(_))
}

case class OffreResponse(id: String,
                         intitule: String,
                         romeCode: String,
                         romeLibelle: String,
                         typeContrat: String,
                         typeContratLibelle: String,
                         description: Option[String],
                         dureeTravailLibelle: String,
                         alternance: Boolean,
                         experienceLibelle: String,
                         experienceExige: ExperienceExigeResponse,
                         trancheEffectifEtab: Option[String],
                         dateActualisation: ZonedDateTime,
                         private val entreprise: Option[EntrepriseResponse],
                         private val salaire: Option[SalaireResponse],
                         private val lieuTravail: LieuTravailResponse,
                         private val contact: Option[ContactResponse],
                         private val competences: List[CompetenceResponse],
                         private val origineOffre: OrigineOffreResponse) {

  val urlOrigine: String = origineOffre.urlOrigine
  val nomContact: Option[String] = contact.flatMap(_.nom)
  val telephoneContact: Option[NumeroTelephone] = contact.flatMap(_.telephone).map(NumeroTelephone(_))
  val emailContact: Option[Email] = contact.flatMap(_.courriel).map(Email)
  val coordonneesContact1: Option[String] = contact.flatMap(_.coordonnees1)
  val coordonneesContact2: Option[String] = contact.flatMap(_.coordonnees2)
  val coordonneesContact3: Option[String] = contact.flatMap(_.coordonnees3)
  val urlPostuler: Option[String] = contact.flatMap(_.urlPostulation)
  val libelleLieuTravail: String = lieuTravail.libelle
  val libelleSalaire: Option[String] = salaire.flatMap(_.libelle)
  val libellesCompetences: List[String] = competences.flatMap(_.libelle)
  val nomEntreprise: Option[String] = entreprise.flatMap(_.nom)
  val descriptionEntreprise: Option[String] = entreprise.flatMap(_.description)
}

object OffreResponse {

  implicit val reads: Reads[OffreResponse] = Json.reads[OffreResponse]
}

case class RechercheOffreRequest(params: List[(String, String)])

case class RechercheOffreResponse(resultats: List[OffreResponse])

class ReferentielOffreWSMapping {

  def buildRechercherOffresRequest(criteresRechercheOffre: CriteresRechercheOffre,
                                   codeINSEE: String): List[RechercheOffreRequest] =
    criteresRechercheOffre.codesROME.sliding(3, 3).toList.map(codesROME =>
      RechercheOffreRequest(List(
        "codeROME" -> codesROME.map(_.value).mkString(","),
        "commune" -> codeINSEE,
        "distance" -> s"${criteresRechercheOffre.rayonRecherche.value}",
        "experience" -> buildExperience(criteresRechercheOffre.experience)
      ))
    )

  def buildOffre(criteresRechercheOffre: CriteresRechercheOffre,
                 offreResponse: OffreResponse): Option[Offre] = {
    val experienceCorrespondante = criteresRechercheOffre.experience match {
      case Experience.DEBUTANT => ExperienceExigeResponse.EXIGE != offreResponse.experienceExige
      case _ => true
    }

    if (experienceCorrespondante)
      Some(Offre(
        id = OffreId(offreResponse.id),
        urlOrigine = offreResponse.urlOrigine,
        intitule = offreResponse.intitule,
        metier = Metier(
          codeROME = CodeROME(offreResponse.romeCode),
          label = offreResponse.romeLibelle
        ),
        libelleLieuTravail = offreResponse.libelleLieuTravail,
        typeContrat = offreResponse.typeContrat,
        libelleTypeContrat = offreResponse.typeContratLibelle,
        libelleDureeTravail = offreResponse.dureeTravailLibelle,
        libelleExperience =
          if (ExperienceExigeResponse.SOUHAITE == offreResponse.experienceExige)
            s"Expérience souhaitée : ${offreResponse.experienceLibelle}"
          else
            offreResponse.experienceLibelle,
        libelleSalaire = offreResponse.libelleSalaire,
        description = offreResponse.description,
        nomEntreprise = offreResponse.nomEntreprise,
        descriptionEntreprise = offreResponse.descriptionEntreprise,
        effectifEntreprise = offreResponse.trancheEffectifEtab,
        competences = offreResponse.libellesCompetences,
        nomContact = offreResponse.nomContact,
        telephoneContact = offreResponse.telephoneContact,
        emailContact = offreResponse.emailContact,
        urlPostuler = offreResponse.urlPostuler,
        coordonneesContact1 = offreResponse.coordonneesContact1,
        coordonneesContact2 = offreResponse.coordonneesContact2,
        coordonneesContact3 = offreResponse.coordonneesContact3,
        dateActualisation = offreResponse.dateActualisation
      )) else None
  }

  def buildExperience(experience: Experience): String = experience match {
    case Experience.DEBUTANT => "1" // Moins d'un an d'experience
    case e@_ => throw new IllegalArgumentException(s"Expérience non gérée : $e")
  }
}
