package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{CodeROME, RayonRecherche}
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CandidatCriteresRechercheDocument(candidatId: CandidatId,
                                             rechercheMetiersEvalues: Option[Boolean],
                                             metiersEvalues: List[CodeROME],
                                             rechercheAutresMetiers: Option[Boolean],
                                             metiersRecherches: List[CodeROME],
                                             codePostal: Option[String],
                                             commune: Option[String],
                                             rayonRecherche: Option[RayonRecherche])

object CandidatCriteresRechercheDocument {

  import CandidatProjectionElasticsearchEsMapping._

  implicit val reads: Reads[CandidatCriteresRechercheDocument] = (
    (JsPath \ candidat_id).read[CandidatId] and
      (JsPath \ recherche_metiers_evalues).readNullable[Boolean] and
      (JsPath \ metiers_evalues).read[List[CodeROME]] and
      (JsPath \ recherche_autres_metiers).readNullable[Boolean] and
      (JsPath \ metiers_recherches).read[List[CodeROME]] and
      (JsPath \ code_postal).readNullable[String] and
      (JsPath \ commune).readNullable[String] and
      (JsPath \ rayon_recherche).readNullable[RayonRecherche]
    ) (CandidatCriteresRechercheDocument.apply _)
}
