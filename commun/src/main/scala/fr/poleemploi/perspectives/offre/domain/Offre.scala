package fr.poleemploi.perspectives.offre.domain

import java.time.ZonedDateTime

import fr.poleemploi.eventsourcing.StringValueObject
import fr.poleemploi.perspectives.commun.domain.{CodeROME, Email, NumeroTelephone}
import play.api.libs.json.{Json, Writes}

case class QualiteProfessionnelle(label: String, description: String)

case class Competence(label: String, exige: Boolean)

case class Permis(label: String, exige: Boolean)

case class Langue(label: String, exige: Boolean)

case class Formation(domaine: Option[String],
                     niveau: Option[String],
                     exige: Boolean)

case class Entreprise(nom: Option[String],
                      description: Option[String],
                      urlLogo: Option[String],
                      urlSite: Option[String],
                      effectif: Option[String],
                      secteurActivite: Option[String])

case class Salaire(libelle: Option[String],
                   commentaire: Option[String],
                   complement1: Option[String],
                   complement2: Option[String])

case class Contact(nom: Option[String],
                   coordonnees1: Option[String],
                   coordonnees2: Option[String],
                   coordonnees3: Option[String],
                   telephone: Option[NumeroTelephone],
                   email: Option[Email],
                   urlPostuler: Option[String])

case class Contrat(code: String, label: String, nature: Option[String])

case class LieuTravail(libelle: Option[String], codePostal: Option[String])

case class ExperienceExige(label: Option[String],
                           commentaire: Option[String],
                           exige: Option[Boolean])

case class OffreId(value: String) extends StringValueObject

case class Offre(id: OffreId,
                 intitule: String,
                 urlOrigine: String,
                 contrat: Contrat,
                 codeROME: Option[CodeROME],
                 description: Option[String],
                 lieuTravail: LieuTravail,
                 libelleDureeTravail: Option[String],
                 complementExercice: Option[String],
                 conditionExercice: Option[String],
                 libelleDeplacement: Option[String],
                 experience: ExperienceExige,
                 competences: List[Competence],
                 qualitesProfessionnelles: List[QualiteProfessionnelle],
                 salaire: Salaire,
                 permis: List[Permis],
                 langues: List[Langue],
                 formations: List[Formation],
                 entreprise: Entreprise,
                 contact: Contact,
                 dateActualisation: ZonedDateTime) {

  override def equals(that: Any): Boolean =
    that match {
      case that: Offre => that.isInstanceOf[Offre] && this.id == that.id
      case _ => false
    }

  override def hashCode: Int =
    31 + (if (id == null) 0 else id.hashCode)
}

object Offre {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writesQualitePro: Writes[QualiteProfessionnelle] = Json.writes[QualiteProfessionnelle]

  implicit val writesCompetence: Writes[Competence] = Json.writes[Competence]

  implicit val writesPermis: Writes[Permis] = Json.writes[Permis]

  implicit val writesLangue: Writes[Langue] = Json.writes[Langue]

  implicit val writesFormation: Writes[Formation] = Json.writes[Formation]

  implicit val writesEntreprise: Writes[Entreprise] = Json.writes[Entreprise]

  implicit val writesSalaire: Writes[Salaire] = Json.writes[Salaire]

  implicit val writesContact: Writes[Contact] = Json.writes[Contact]

  implicit val writesContrat: Writes[Contrat] = Json.writes[Contrat]

  implicit val writesLieuTravail: Writes[LieuTravail] = Json.writes[LieuTravail]

  implicit val writesExperienceExige: Writes[ExperienceExige] = Json.writes[ExperienceExige]

  implicit val writesOffre: Writes[Offre] = Json.writes[Offre]
}