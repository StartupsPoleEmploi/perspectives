package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.candidat.cv.domain.CVId
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CandidatPourRechercheOffreDocument(metiersValides: Set[MetierValideDocument],
                                              criteresRecherche: CandidatCriteresRechercheDocument,
                                              cvId: Option[CVId])

object CandidatPourRechercheOffreDocument {

  import CandidatProjectionElasticsearchMapping._
  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val reads: Reads[CandidatPourRechercheOffreDocument] = (
    (JsPath \ metiers_valides).read[Set[MetierValideDocument]] and
      (JsPath \ criteres_recherche).read[CandidatCriteresRechercheDocument] and
      (JsPath \ cv_id).readNullable[CVId]
    ) (CandidatPourRechercheOffreDocument.apply _)
}