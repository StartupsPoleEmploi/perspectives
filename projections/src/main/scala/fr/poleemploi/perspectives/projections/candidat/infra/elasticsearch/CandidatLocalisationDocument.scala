package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CandidatLocalisationDocument(commune: Option[String],
                                        codePostal: Option[String],
                                        latitude: Option[Double],
                                        longitude: Option[Double])

object CandidatLocalisationDocument {

  import CandidatProjectionElasticsearchMapping._

  implicit val reads: Reads[CandidatLocalisationDocument] = (
    (JsPath \ commune).readNullable[String] and
      (JsPath \ code_postal).readNullable[String] and
      (JsPath \ latitude).readNullable[Double] and
      (JsPath \ longitude).readNullable[Double]
    ) (CandidatLocalisationDocument.apply _)
}
