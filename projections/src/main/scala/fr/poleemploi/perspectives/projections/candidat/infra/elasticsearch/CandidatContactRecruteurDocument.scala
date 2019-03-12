package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CandidatContactRecruteurDocument(contactRecruteur: Option[Boolean],
                                            contactFormation: Option[Boolean])

object CandidatContactRecruteurDocument {

  import CandidatProjectionElasticsearchMapping._

  implicit val reads: Reads[CandidatContactRecruteurDocument] = (
    (JsPath \ contact_recruteur).readNullable[Boolean] and
      (JsPath \ contact_formation).readNullable[Boolean]
    ) (CandidatContactRecruteurDocument.apply _)
}
