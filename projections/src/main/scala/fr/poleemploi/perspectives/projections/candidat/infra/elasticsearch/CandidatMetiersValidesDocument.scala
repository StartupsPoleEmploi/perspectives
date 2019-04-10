package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.commun.domain.{CodeROME, Habilete}
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CandidatMetiersValidesDocument(metiersValides: Set[CodeROME],
                                          habiletes: Set[Habilete])

object CandidatMetiersValidesDocument {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
  import CandidatProjectionElasticsearchMapping._

  implicit val reads: Reads[CandidatMetiersValidesDocument] = (
    (JsPath \ metiers_valides).read[Set[CodeROME]] and
      (JsPath \ habiletes).read[Set[Habilete]]
    ) (CandidatMetiersValidesDocument.apply _)
}