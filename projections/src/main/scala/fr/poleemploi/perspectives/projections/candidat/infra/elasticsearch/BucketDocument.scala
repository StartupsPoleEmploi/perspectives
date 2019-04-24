package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class BucketDocument(key: String,
                          count: Int)

object BucketDocument {

  implicit val reads: Reads[BucketDocument] = (
    (JsPath \ "key").read[String] and
      (JsPath \ "doc_count").read[Int]
    ) (BucketDocument.apply _)
}
