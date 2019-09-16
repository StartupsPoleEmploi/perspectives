package fr.poleemploi.perspectives.offre.infra.ws

import java.time.LocalDateTime
import java.util.regex.Pattern

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

class ReferentielOffreWSMapping {

  // Maximums des index pour la recherche d'offres
  private val maxRangeStart = 1000
  private val maxRangeEnd = 1149

  // On a pas le nombre de résultats dans un champ mais dans un header du type "offres debut-fin/total"
  private val contentRangePattern: Pattern = Pattern.compile(".*\\s(\\d+)-(\\d+)/(\\d+)")

  /**
    * l'API ne permet de passer que peu de filtres pour l'instant (que 3 codeROME par appels, deux secteurActivite par appel, etc.) : on fait donc plusieurs filtres à postériori
    */
  def buildRechercherOffresRequest(criteresRechercheOffre: CriteresRechercheOffre,
                                   codeINSEE: Option[String]): List[(String, String)] = List(
    criteresRechercheOffre.motsCles
      .map(_.replaceAll("[^\\w&&[^,]&&[^']]", "").replaceAll(",", " "))
      .filter(_.length >= 2)
      .take(7) match {
      case Nil => None
      case l@_ => Some("motsCles", l.mkString(","))
    },
    codeINSEE.map(c => "commune" -> c),
    codeINSEE.flatMap(_ => criteresRechercheOffre.rayonRecherche.map(r => "distance" ->
      (r match {
        case r@_ if UniteLongueur.KM == r.uniteLongueur => s"${r.value}"
        case _ => throw new IllegalArgumentException(s"Rayon de recherche non géré : $r")
      })
    )),
    criteresRechercheOffre.typesContrats match {
      case Nil => None
      case l@_ => Some("typeContrat" -> l.map(_.value).mkString(","))
    },
    criteresRechercheOffre.codesROME match {
      case Nil => None
      case l@_ if l.size <= 3 => Some("codeROME" -> l.map(_.value).mkString(","))
      case _ => None
    },
    Some("experience" -> (criteresRechercheOffre.experience match {
      case Experience.DEBUTANT => "1" // Moins d'un an d'experience
      case e@_ => throw new IllegalArgumentException(s"Expérience non gérée : ${e.value}")
    })),
    Some("sort" -> "0"),
    Some("origineOffre" -> "1"),
    criteresRechercheOffre.page.map(p => ("range", s"${p.debut}-${p.fin}"))
  ).flatten

  /**
    * l'API ne permet de passer que peu de filtres pour l'instant (que 3 codeROME par appels, deux secteurActivite par appel, etc.) : on fait donc plusieurs filtres à postériori. <br />
    * <ul>
    * <li>Si l'experience est DEBUTANT cela signifie moins d'un an d'expérience côté API, on doit donc quand même vérifier qu'il n'y ait pas d'expérience exigée</li>
    * <li>Si l'experience est DEBUTANT, malgré le filtre sur l'expérience l'offre peut aussi contenir des formations exigées</li>
    * <li>l'API ne permet pas de passer beaucoup de codeROME, on filtre donc à postériori sur les secteurs, domaines ou codeROME</li>
    * <li>on exclue certains codes ROME tels que N41 (Transport routier) qui pose pas mal de soucis</li>
    * </ul>
    */
  def filterOffresResponses(criteresRechercheOffre: CriteresRechercheOffre,
                            offres: List[OffreResponse]): List[OffreResponse] =
    offres.filter(o =>
      (Experience.DEBUTANT != criteresRechercheOffre.experience || (!o.experienceExige.contains(ExperienceExigeResponse.EXIGE) && !o.formations.exists(f => ExigenceResponse.EXIGE == f.exigence))) &&
        o.romeCode.exists(r =>
          (criteresRechercheOffre.codesROME.isEmpty || criteresRechercheOffre.codesROME.exists(c => r.startsWith(c.value))) &&
            (criteresRechercheOffre.secteursActivites.isEmpty || criteresRechercheOffre.secteursActivites.exists(c => r.startsWith(c.value))) &&
            (criteresRechercheOffre.codesDomaineProfessionnels.isEmpty || criteresRechercheOffre.codesDomaineProfessionnels.exists(c => r.startsWith(c.value))) &&
            !r.startsWith("N41")
        )
    )

  def buildPageOffres(contentRange: Option[String], acceptRange: Option[String]): Option[PageOffres] =
    for {
      nbOffresParPage <- acceptRange.map(_.toInt)
      matcher = contentRangePattern.matcher(contentRange.getOrElse(""))
      contentRange <- contentRange if matcher.matches()
      debut = matcher.group(1).toInt + nbOffresParPage
      fin = matcher.group(2).toInt + nbOffresParPage
      nbOffresTotal = matcher.group(3).toInt
      page <- Some(PageOffres(debut = debut, fin = fin)) if debut < maxRangeStart && fin < maxRangeEnd && fin < (nbOffresTotal - 1)
    } yield page


  def buildOffre(offreResponse: OffreResponse): Offre =
    Offre(
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
        urlLogo = offreResponse.logoEntreprise.map(l =>
          if (l.startsWith("http")) l
          else s"https://entreprise.pole-emploi.fr/static/img/logos/$l.png"
        ),
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
    )
}
