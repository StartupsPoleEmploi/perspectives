package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CandidatRechercheDocument(candidatId: CandidatId,
                                     nom: Nom,
                                     prenom: Prenom,
                                     email: Email,
                                     metiersEvalues: List[CodeROME],
                                     habiletes: List[Habilete],
                                     metiersRecherches: List[CodeROME],
                                     numeroTelephone: NumeroTelephone,
                                     rayonRecherche: RayonRecherche,
                                     commune: String,
                                     cvId: Option[CVId],
                                     cvTypeMedia: Option[TypeMedia])

object CandidatRechercheDocument {

  import CandidatProjectionElasticsearchEsMapping._

  implicit val reads: Reads[CandidatRechercheDocument] = (
    (JsPath \ candidat_id).read[CandidatId] and
      (JsPath \ nom).read[Nom] and
      (JsPath \ prenom).read[Prenom] and
      (JsPath \ email).read[Email] and
      (JsPath \ metiers_evalues).read[List[CodeROME]] and
      (JsPath \ habiletes).read[List[Habilete]] and
      (JsPath \ metiers_recherches).read[List[CodeROME]] and
      (JsPath \ numero_telephone).read[NumeroTelephone] and
      (JsPath \ rayon_recherche).read[RayonRecherche] and
      (JsPath \ commune).read[String] and
      (JsPath \ cv_id).readNullable[CVId] and
      (JsPath \ cv_type_media).readNullable[TypeMedia]
    ) (CandidatRechercheDocument.apply _)

}
