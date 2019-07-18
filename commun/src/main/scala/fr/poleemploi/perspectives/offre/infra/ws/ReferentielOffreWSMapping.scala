package fr.poleemploi.perspectives.offre.infra.ws

import java.time.LocalDateTime

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.offre.domain._
import play.api.libs.json._

case class ExigenceResponse(value: String)

object ExigenceResponse {

  val SOUHAITE = ExigenceResponse(value = "S")
  val EXIGE = ExigenceResponse(value = "E")

  implicit val reads: Reads[ExigenceResponse] =
    __.read[String].map(ExigenceResponse(_))
}

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

case class CompetenceResponse(libelle: String,
                              code: Option[String],
                              exigence: ExigenceResponse)

object CompetenceResponse {

  implicit val reads: Reads[CompetenceResponse] = Json.reads[CompetenceResponse]
}

case class QualiteProfessionnelleResponse(libelle: String,
                                          description: String)

object QualiteProfessionnelleResponse {

  implicit val reads: Reads[QualiteProfessionnelleResponse] = Json.reads[QualiteProfessionnelleResponse]
}

case class PermisResponse(libelle: String,
                          exigence: ExigenceResponse)

object PermisResponse {

  implicit val reads: Reads[PermisResponse] = Json.reads[PermisResponse]
}

case class FormationResponse(niveauLibelle: Option[String],
                             domaineLibelle: Option[String],
                             commentaire: Option[String],
                             exigence: ExigenceResponse)

object FormationResponse {

  implicit val reads: Reads[FormationResponse] = Json.reads[FormationResponse]
}

case class LangueResponse(libelle: String,
                          exigence: ExigenceResponse)

object LangueResponse {

  implicit val reads: Reads[LangueResponse] = Json.reads[LangueResponse]
}

case class SalaireResponse(libelle: Option[String],
                           commentaire: Option[String],
                           complement1: Option[String],
                           complement2: Option[String])

object SalaireResponse {

  implicit val reads: Reads[SalaireResponse] = Json.reads[SalaireResponse]
}

case class LieuTravailResponse(codePostal: Option[String],
                               libelle: Option[String])

object LieuTravailResponse {

  implicit val reads: Reads[LieuTravailResponse] = Json.reads[LieuTravailResponse]
}

case class EntrepriseResponse(nom: Option[String],
                              description: Option[String],
                              logo: Option[String],
                              url: Option[String])

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
    __.read[String].map(ExperienceExigeResponse(_))
}

case class OffreResponse(id: String,
                         intitule: String,
                         romeCode: Option[String],
                         romeLibelle: Option[String],
                         typeContrat: String,
                         typeContratLibelle: String,
                         natureContrat: Option[String],
                         description: Option[String],
                         dureeTravailLibelle: Option[String],
                         alternance: Boolean,
                         experienceLibelle: Option[String],
                         experienceCommentaire: Option[String],
                         experienceExige: Option[ExperienceExigeResponse],
                         trancheEffectifEtab: Option[String],
                         complementExercice: Option[String],
                         conditionExercice: Option[String],
                         deplacementLibelle: Option[String],
                         secteurActiviteLibelle: Option[String],
                         competences: List[CompetenceResponse],
                         qualitesProfessionnelles: List[QualiteProfessionnelleResponse],
                         permis: List[PermisResponse],
                         formations: List[FormationResponse],
                         langues: List[LangueResponse],
                         dateActualisation: LocalDateTime,
                         private val entreprise: Option[EntrepriseResponse],
                         private val salaire: Option[SalaireResponse],
                         private val lieuTravail: Option[LieuTravailResponse],
                         private val contact: Option[ContactResponse],
                         private val origineOffre: OrigineOffreResponse) {

  val urlOrigine: String = origineOffre.urlOrigine
  val nomContact: Option[String] = contact.flatMap(_.nom)
  val telephoneContact: Option[NumeroTelephone] = contact.flatMap(_.telephone).map(NumeroTelephone(_))
  val emailContact: Option[Email] = contact.flatMap(_.courriel).map(Email(_))
  val coordonneesContact1: Option[String] = contact.flatMap(_.coordonnees1)
  val coordonneesContact2: Option[String] = contact.flatMap(_.coordonnees2)
  val coordonneesContact3: Option[String] = contact.flatMap(_.coordonnees3)
  val urlPostuler: Option[String] = contact.flatMap(_.urlPostulation)
  val libelleLieuTravail: Option[String] = lieuTravail.flatMap(_.libelle)
  val codePostalLieuTravail: Option[String] = lieuTravail.flatMap(_.codePostal)
  val libelleSalaire: Option[String] = salaire.flatMap(_.libelle)
  val commentaireSalaire: Option[String] = salaire.flatMap(_.commentaire)
  val complement1Salaire: Option[String] = salaire.flatMap(_.complement1)
  val complement2Salaire: Option[String] = salaire.flatMap(_.complement2)
  val nomEntreprise: Option[String] = entreprise.flatMap(_.nom)
  val descriptionEntreprise: Option[String] = entreprise.flatMap(_.description)
  val logoEntreprise: Option[String] = entreprise.flatMap(_.logo)
  val urlEntreprise: Option[String] = entreprise.flatMap(_.url)
}

object OffreResponse {

  implicit val reads: Reads[OffreResponse] = (json: JsValue) => JsSuccess(
    OffreResponse(
      id = (json \ "id").as[String],
      intitule = (json \ "intitule").as[String],
      romeCode = (json \ "romeCode").asOpt[String],
      romeLibelle = (json \ "romeLibelle").asOpt[String],
      typeContrat = (json \ "typeContrat").as[String],
      typeContratLibelle = (json \ "typeContratLibelle").as[String],
      natureContrat = (json \ "natureContrat").asOpt[String],
      description = (json \ "description").asOpt[String],
      dureeTravailLibelle = (json \ "dureeTravailLibelle").asOpt[String],
      alternance = (json \ "alternance").as[Boolean],
      experienceExige = (json \ "experienceExige").asOpt[ExperienceExigeResponse],
      experienceCommentaire = (json \ "experienceCommentaire").asOpt[String],
      experienceLibelle = (json \ "experienceLibelle").asOpt[String],
      complementExercice = (json \ "complementExercice").asOpt[String],
      conditionExercice = (json \ "conditionExercice").asOpt[String],
      deplacementLibelle = (json \ "deplacementLibelle").asOpt[String],
      secteurActiviteLibelle = (json \ "secteurActiviteLibelle").asOpt[String],
      trancheEffectifEtab = (json \ "trancheEffectifEtab").asOpt[String],
      competences = (json \ "competences").orElse(JsDefined(JsArray.empty)).as[List[CompetenceResponse]],
      qualitesProfessionnelles = (json \ "qualitesProfessionnelles").orElse(JsDefined(JsArray.empty)).as[List[QualiteProfessionnelleResponse]],
      permis = (json \ "permis").orElse(JsDefined(JsArray.empty)).as[List[PermisResponse]],
      formations = (json \ "formations").orElse(JsDefined(JsArray.empty)).as[List[FormationResponse]],
      langues = (json \ "langues").orElse(JsDefined(JsArray.empty)).as[List[LangueResponse]],
      dateActualisation = (json \ "dateActualisation").as[LocalDateTime],
      entreprise = (json \ "entreprise").asOpt[EntrepriseResponse],
      salaire = (json \ "salaire").asOpt[SalaireResponse],
      lieuTravail = (json \ "lieuTravail").asOpt[LieuTravailResponse],
      contact = (json \ "contact").asOpt[ContactResponse],
      origineOffre = (json \ "origineOffre").as[OrigineOffreResponse]
    )
  )
}

case class RechercheOffreRequest(params: List[(String, String)])

class ReferentielOffreWSMapping {

  def buildRechercherOffresRequest(criteresRechercheOffre: CriteresRechercheOffre,
                                   codeINSEE: Option[String]): RechercheOffreRequest =
    RechercheOffreRequest(List(
      criteresRechercheOffre.motCle.map(m => "motsCles" -> m),
      codeINSEE.map(c => "commune" -> c),
      codeINSEE.flatMap(_ => criteresRechercheOffre.rayonRecherche.map(r => "distance" -> s"${r.value}")),
      criteresRechercheOffre.typesContrats match {
        case Nil => None
        case l@_ => Some("typeContrat" -> l.map(_.value).mkString(","))
      },
      criteresRechercheOffre.codesROME match {
        case Nil => None
        case l@_ => Some("codeROME" -> l.map(_.value).mkString(","))
      },
      Some("experience" -> buildExperience(criteresRechercheOffre.experience))
    ).flatten)

  def buildOffre(criteresRechercheOffre: CriteresRechercheOffre,
                 offreResponse: OffreResponse): Option[Offre] = {
    val experienceCorrespondante = criteresRechercheOffre.experience match {
      case Experience.DEBUTANT => !offreResponse.experienceExige.contains(ExperienceExigeResponse.EXIGE)
      case _ => true
    }
    val secteurActiviteCorrespondant = criteresRechercheOffre.secteursActivites match {
      case Nil => true
      case xs => xs.exists(s => offreResponse.romeCode.exists(r => r.startsWith(s.value)))
    }
    val sansFormationExigee = !offreResponse.formations.exists(f => ExigenceResponse.EXIGE == f.exigence)

    if (experienceCorrespondante && secteurActiviteCorrespondant && sansFormationExigee)
      Some(Offre(
        id = OffreId(offreResponse.id),
        urlOrigine = offreResponse.urlOrigine,
        intitule = offreResponse.intitule,
        codeROME = offreResponse.romeCode.map(CodeROME),
        contrat = Contrat(
          code = offreResponse.typeContrat,
          label = offreResponse.typeContratLibelle,
          nature = offreResponse.natureContrat
        ),
        description = offreResponse.description,
        lieuTravail = LieuTravail(libelle = offreResponse.libelleLieuTravail, codePostal = offreResponse.codePostalLieuTravail),
        libelleDureeTravail = offreResponse.dureeTravailLibelle,
        complementExercice = offreResponse.complementExercice,
        conditionExercice = offreResponse.conditionExercice,
        libelleDeplacement = offreResponse.deplacementLibelle,
        experience = ExperienceExige(
          label =
            if (offreResponse.experienceExige.contains(ExperienceExigeResponse.SOUHAITE))
              offreResponse.experienceLibelle.map(l => s"Expérience souhaitée : $l")
            else
              offreResponse.experienceLibelle,
          commentaire = offreResponse.experienceCommentaire,
          exige =
            if (offreResponse.experienceExige.contains(ExperienceExigeResponse.EXIGE))
              Some(true)
            else
              Some(false)
        ),
        competences = offreResponse.competences.map(c => Competence(
          label = c.libelle,
          exige = ExigenceResponse.EXIGE == c.exigence
        )),
        qualitesProfessionnelles = offreResponse.qualitesProfessionnelles.map(q => QualiteProfessionnelle(
          label = q.libelle,
          description = q.description
        )),
        salaire = Salaire(
          libelle = offreResponse.libelleSalaire,
          commentaire = offreResponse.commentaireSalaire,
          complement1 = offreResponse.complement1Salaire,
          complement2 = offreResponse.complement2Salaire
        ),
        permis = offreResponse.permis.map(p => Permis(
          label = p.libelle,
          exige = ExigenceResponse.EXIGE == p.exigence
        )),
        langues = offreResponse.langues.map(l => Langue(
          label = l.libelle,
          exige = ExigenceResponse.EXIGE == l.exigence
        )),
        formations = offreResponse.formations.map(f => Formation(
          domaine = f.domaineLibelle,
          niveau = f.niveauLibelle,
          exige = ExigenceResponse.EXIGE == f.exigence
        )),
        entreprise = Entreprise(
          nom = offreResponse.nomEntreprise,
          description = offreResponse.descriptionEntreprise,
          urlLogo = offreResponse.logoEntreprise.map(l => s"https://entreprise.pole-emploi.fr/static/img/logos/$l.png"),
          urlSite = offreResponse.urlEntreprise,
          effectif = offreResponse.trancheEffectifEtab,
          secteurActivite = offreResponse.secteurActiviteLibelle
        ),
        contact = Contact(
          nom = offreResponse.nomContact,
          coordonnees1 = offreResponse.coordonneesContact1,
          coordonnees2 = offreResponse.coordonneesContact2,
          coordonnees3 = offreResponse.coordonneesContact3,
          telephone = offreResponse.telephoneContact,
          email = offreResponse.emailContact,
          urlPostuler = offreResponse.urlPostuler
        ),
        dateActualisation = offreResponse.dateActualisation
      )) else None
  }

  def buildExperience(experience: Experience): String = experience match {
    case Experience.DEBUTANT => "1" // Moins d'un an d'experience
    case e@_ => throw new IllegalArgumentException(s"Expérience non gérée : $e")
  }
}
