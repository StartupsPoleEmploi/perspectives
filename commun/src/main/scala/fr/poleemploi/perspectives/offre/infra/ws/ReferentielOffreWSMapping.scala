package fr.poleemploi.perspectives.offre.infra.ws

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.commun.domain.{CodeROME, Email, Metier, NumeroTelephone}
import fr.poleemploi.perspectives.offre.domain.{CriteresRechercheOffre, Experience, Offre, OffreId}
import play.api.libs.json.{Json, Reads}

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

case class OffreResponse(id: String,
                         intitule: String,
                         romeCode: String,
                         romeLibelle: String,
                         typeContrat: String,
                         typeContratLibelle: String,
                         description: Option[String],
                         dureeTravailLibelle: String,
                         experienceLibelle: String,
                         entreprise: Option[EntrepriseResponse],
                         trancheEffectifEtab: Option[String],
                         salaire: Option[SalaireResponse],
                         lieuTravail: LieuTravailResponse,
                         contact: Option[ContactResponse],
                         competences: List[CompetenceResponse],
                         origineOffre: OrigineOffreResponse,
                         dateActualisation: ZonedDateTime)

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

  def buildOffre(offreResponse: OffreResponse): Offre =
    Offre(
      id = OffreId(offreResponse.id),
      urlOrigine = offreResponse.origineOffre.urlOrigine,
      intitule = offreResponse.intitule,
      metier = Metier(codeROME = CodeROME(offreResponse.romeCode), offreResponse.romeLibelle),
      libelleLieuTravail = offreResponse.lieuTravail.libelle,
      typeContrat = offreResponse.typeContrat,
      libelleTypeContrat = offreResponse.typeContratLibelle,
      libelleDureeTravail = offreResponse.dureeTravailLibelle,
      libelleExperience = offreResponse.experienceLibelle,
      libelleSalaire = offreResponse.salaire.flatMap(_.libelle),
      description = offreResponse.description,
      nomEntreprise = offreResponse.entreprise.flatMap(_.nom),
      descriptionEntreprise = offreResponse.entreprise.flatMap(_.description),
      effectifEntreprise = offreResponse.trancheEffectifEtab,
      competences = offreResponse.competences.flatMap(_.libelle),
      nomContact = offreResponse.contact.flatMap(_.nom),
      telephoneContact = offreResponse.contact.flatMap(_.telephone).map(NumeroTelephone(_)),
      emailContact = offreResponse.contact.flatMap(_.courriel).map(Email),
      urlPostuler = offreResponse.contact.flatMap(_.urlPostulation),
      coordonneesContact = for {
        contact <- offreResponse.contact
        coordonnees1 <- contact.coordonnees1
        coordonnees2 <- contact.coordonnees2
        coordonnees3 <- contact.coordonnees3
      } yield s"$coordonnees1 $coordonnees2 $coordonnees3",
      dateActualisation = offreResponse.dateActualisation.toLocalDateTime
    )

  def buildExperience(experience: Experience): String = experience match {
    case Experience.DEBUTANT => "1"
    case e@_ => throw new IllegalArgumentException(s"Expérience non gérée : $e")
  }
}
