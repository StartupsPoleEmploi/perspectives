package fr.poleemploi.perspectives.rome.infra.elasticsearch

import play.api.libs.json.{Json, Writes}

case class BulkIndexDocument(index: BulkIndexMetadataDocument)

object BulkIndexDocument {
  implicit val writesBulkIndexMetadataDocument = BulkIndexMetadataDocument.writes
  implicit val writes: Writes[BulkIndexDocument] = Json.writes[BulkIndexDocument]
}

case class BulkIndexMetadataDocument(_index: String,
                                     _type: String,
                                     _id: String)

object BulkIndexMetadataDocument {
  implicit val writes: Writes[BulkIndexMetadataDocument] = Json.writes[BulkIndexMetadataDocument]
}


