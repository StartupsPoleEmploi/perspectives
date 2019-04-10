package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CandidatLocalisationDocument(commune: String,
                                        codePostal: String,
                                        latitude: Option[Double],
                                        longitude: Option[Double])

object CandidatLocalisationDocument {

  import CandidatProjectionElasticsearchMapping._

  implicit val reads: Reads[CandidatLocalisationDocument] = (
    (JsPath \ commune).read[String] and
      (JsPath \ code_postal).read[String] and
      (JsPath \ latitude).readNullable[Double] and
      (JsPath \ longitude).readNullable[Double]
    ) (CandidatLocalisationDocument.apply _)
}
