package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CandidatPourConseillerBatchDisponibilitesDocument(candidatId: CandidatId,
                                                             email: Email)

object CandidatPourConseillerBatchDisponibilitesDocument {

  import CandidatProjectionElasticsearchMapping._
  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val reads: Reads[CandidatPourConseillerBatchDisponibilitesDocument] = (
    (JsPath \ candidat_id).read[CandidatId] and
      (JsPath \ email).read[Email]
    ) (CandidatPourConseillerBatchDisponibilitesDocument.apply _)
}
