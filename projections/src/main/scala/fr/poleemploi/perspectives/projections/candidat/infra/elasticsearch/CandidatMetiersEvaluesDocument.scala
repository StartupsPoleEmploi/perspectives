package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.commun.domain.{CodeROME, Habilete}
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CandidatMetiersEvaluesDocument(metiersEvalues: List[CodeROME],
                                          habiletes: List[Habilete])

object CandidatMetiersEvaluesDocument {

  import CandidatProjectionElasticsearchEsMapping._

  implicit val reads: Reads[CandidatMetiersEvaluesDocument] = (
    (JsPath \ metiers_evalues).read[List[CodeROME]] and
      (JsPath \ habiletes).read[List[Habilete]]
    ) (CandidatMetiersEvaluesDocument.apply _)
}