package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CandidatPourConseillerBatchJVRDocument(candidatId: CandidatId,
                                                  email: Email,
                                                  nom: Nom,
                                                  prenom: Prenom)

object CandidatPourConseillerBatchJVRDocument {

  import CandidatProjectionElasticsearchMapping._
  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val reads: Reads[CandidatPourConseillerBatchJVRDocument] = (
    (JsPath \ candidat_id).read[CandidatId] and
      (JsPath \ email).read[Email] and
      (JsPath \ nom).read[Nom] and
      (JsPath \ prenom).read[Prenom]
    ) (CandidatPourConseillerBatchJVRDocument.apply _)
}
