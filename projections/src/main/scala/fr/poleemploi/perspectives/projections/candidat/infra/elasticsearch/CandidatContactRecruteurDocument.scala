package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.projections.candidat.CandidatContactRecruteurDto
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CandidatContactRecruteurDocument(contacteParAgenceInterim: Option[Boolean],
                                            contacteParOrganismeFormation: Option[Boolean]) {

  def toContactRecruteurDto: CandidatContactRecruteurDto =
    CandidatContactRecruteurDto(
      contacteParAgenceInterim = contacteParAgenceInterim,
      contacteParOrganismeFormation = contacteParOrganismeFormation
    )
}

object CandidatContactRecruteurDocument {

  import CandidatProjectionElasticsearchEsMapping._

  implicit val reads: Reads[CandidatContactRecruteurDocument] = (
    (JsPath \ contacte_par_agence_interim).readNullable[Boolean] and
      (JsPath \ contacte_par_organisme_formation).readNullable[Boolean]
    ) (CandidatContactRecruteurDocument.apply _)
}
