package fr.poleemploi.perspectives.metier.infra.elasticsearch

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class SousSecteurDocument(code: String,
                               label: String)

object SousSecteurDocument {

  implicit val reads: Reads[SousSecteurDocument] = (
    (JsPath \ "code").read[String] and
      (JsPath \ "label").read[String]
    ) (SousSecteurDocument.apply _)
}