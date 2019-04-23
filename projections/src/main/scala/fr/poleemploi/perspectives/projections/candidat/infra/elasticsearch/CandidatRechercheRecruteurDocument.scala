package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.commun.domain._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CandidatRechercheRecruteurDocument(candidatId: CandidatId,
                                              nom: Nom,
                                              prenom: Prenom,
                                              email: Email,
                                              numeroTelephone: NumeroTelephone,
                                              metiersValides: List[CodeROME],
                                              habiletes: Set[Habilete],
                                              metiersValidesRecherches: List[CodeROME],
                                              metiersRecherches: List[CodeROME],
                                              communeRecherche: String,
                                              rayonRecherche: Option[RayonRechercheDocument],
                                              cvId: Option[CVId],
                                              cvTypeMedia: Option[TypeMedia])

object CandidatRechercheRecruteurDocument {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
  import CandidatProjectionElasticsearchMapping._

  implicit val reads: Reads[CandidatRechercheRecruteurDocument] = (
    (JsPath \ candidat_id).read[CandidatId] and
      (JsPath \ nom).read[Nom] and
      (JsPath \ prenom).read[Prenom] and
      (JsPath \ email).read[Email] and
      (JsPath \ numero_telephone).read[NumeroTelephone] and
      (JsPath \ metiers_valides).read[List[CodeROME]] and
      (JsPath \ habiletes).read[Set[Habilete]] and
      (JsPath \ criteres_recherche \ "metiers_valides").read[List[CodeROME]] and
      (JsPath \ criteres_recherche \ "metiers").read[List[CodeROME]] and
      (JsPath \ criteres_recherche \ "commune").read[String] and
      (JsPath \ criteres_recherche \ "rayon").readNullable[RayonRechercheDocument] and
      (JsPath \ cv_id).readNullable[CVId] and
      (JsPath \ cv_type_media).readNullable[TypeMedia]
    ) (CandidatRechercheRecruteurDocument.apply _)

}
