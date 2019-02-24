package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.CVId
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CandidatPourRechercheOffreDocument(candidatId: CandidatId,
                                              metiersEvalues: List[CodeROME],
                                              commune: Option[String],
                                              codePostal: Option[String],
                                              rayonRecherche: Option[RayonRecherche],
                                              cvId: Option[CVId])

object CandidatPourRechercheOffreDocument {

  import CandidatProjectionElasticsearchEsMapping._

  implicit val reads: Reads[CandidatPourRechercheOffreDocument] = (
    (JsPath \ candidat_id).read[CandidatId] and
      (JsPath \ metiers_evalues).read[List[CodeROME]] and
      (JsPath \ commune).readNullable[String] and
      (JsPath \ code_postal).readNullable[String] and
      (JsPath \ rayon_recherche).readNullable[RayonRecherche] and
      (JsPath \ cv_id).readNullable[CVId]
    ) (CandidatPourRechercheOffreDocument.apply _)
}