package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.commun.domain._
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class KeysetCandidatPourRecruteurDocument(score: Option[Int],
                                               dateInscription: Long,
                                               candidatId: CandidatId)

object KeysetCandidatPourRecruteurDocument {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[KeysetCandidatPourRecruteurDocument] =
    k => k.score.map(s =>
      Json.arr(s, k.dateInscription, k.candidatId)
    ).getOrElse(
      Json.arr(k.dateInscription, k.candidatId)
    )
}

case class CandidatPourRecruteurDocument(candidatId: CandidatId,
                                         nom: Nom,
                                         prenom: Prenom,
                                         email: Email,
                                         numeroTelephone: NumeroTelephone,
                                         metiersValides: List[MetierValideDocument],
                                         metiersValidesRecherches: List[CodeROME],
                                         metiersRecherches: List[CodeROME],
                                         communeRecherche: String,
                                         codePostalRecherche: String,
                                         rayonRecherche: Option[RayonRechercheDocument],
                                         tempsTravailRecherche: Option[TempsTravail],
                                         cvId: Option[CVId],
                                         cvTypeMedia: Option[TypeMedia],
                                         centresInteret: List[CentreInteret],
                                         langues: List[Langue],
                                         permis: List[Permis],
                                         savoirEtre: List[SavoirEtre],
                                         savoirFaire: List[SavoirFaire],
                                         formations: List[FormationDocument],
                                         experiencesProfessionnelles: List[ExperienceProfessionnelleDocument])

object CandidatPourRecruteurDocument {

  import CandidatProjectionElasticsearchMapping._
  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val reads: Reads[CandidatPourRecruteurDocument] = (
    (JsPath \ candidat_id).read[CandidatId] and
      (JsPath \ nom).read[Nom] and
      (JsPath \ prenom).read[Prenom] and
      (JsPath \ email).read[Email] and
      (JsPath \ numero_telephone).read[NumeroTelephone] and
      (JsPath \ metiers_valides).read[List[MetierValideDocument]] and
      (JsPath \ criteres_recherche \ "metiers_valides").read[List[CodeROME]] and
      (JsPath \ criteres_recherche \ "metiers").read[List[CodeROME]] and
      (JsPath \ criteres_recherche \ "commune").read[String] and
      (JsPath \ criteres_recherche \ "code_postal").read[String] and
      (JsPath \ criteres_recherche \ "rayon").readNullable[RayonRechercheDocument] and
      (JsPath \ criteres_recherche \ "temps_travail").readNullable[TempsTravail] and
      (JsPath \ cv_id).readNullable[CVId] and
      (JsPath \ cv_type_media).readNullable[TypeMedia] and
      (JsPath \ centres_interet).read[List[CentreInteret]] and
      (JsPath \ langues).read[List[Langue]] and
      (JsPath \ permis).read[List[Permis]] and
      (JsPath \ savoir_etre).read[List[SavoirEtre]] and
      (JsPath \ savoir_faire).read[List[SavoirFaire]] and
      (JsPath \ formations).read[List[FormationDocument]] and
      (JsPath \ experiences_professionnelles).read[List[ExperienceProfessionnelleDocument]]
    ) (CandidatPourRecruteurDocument.apply _)
}
