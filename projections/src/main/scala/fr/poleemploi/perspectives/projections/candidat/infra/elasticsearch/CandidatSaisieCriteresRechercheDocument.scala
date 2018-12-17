package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.commun.domain.{CodeROME, NumeroTelephone, RayonRecherche}
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CandidatSaisieCriteresRechercheDocument(candidatId: CandidatId,
                                                   nom: String,
                                                   prenom: String,
                                                   rechercheMetierEvalue: Option[Boolean],
                                                   metiersEvalues: List[CodeROME],
                                                   rechercheAutreMetier: Option[Boolean],
                                                   metiersRecherches: List[CodeROME],
                                                   contacteParAgenceInterim: Option[Boolean],
                                                   contacteParOrganismeFormation: Option[Boolean],
                                                   rayonRecherche: Option[RayonRecherche],
                                                   numeroTelephone: Option[NumeroTelephone],
                                                   cvId: Option[CVId],
                                                   cvTypeMedia: Option[TypeMedia])

object CandidatSaisieCriteresRechercheDocument {

  import CandidatProjectionElasticsearchEsMapping._

  implicit val reads: Reads[CandidatSaisieCriteresRechercheDocument] = (
    (JsPath \ candidat_id).read[CandidatId] and
      (JsPath \ nom).read[String] and
      (JsPath \ prenom).read[String] and
      (JsPath \ recherche_metiers_evalues).readNullable[Boolean] and
      (JsPath \ metiers_evalues).read[List[CodeROME]] and
      (JsPath \ recherche_autres_metiers).readNullable[Boolean] and
      (JsPath \ metiers_recherches).read[List[CodeROME]] and
      (JsPath \ contacte_par_agence_interim).readNullable[Boolean] and
      (JsPath \ contacte_par_organisme_formation).readNullable[Boolean] and
      (JsPath \ rayon_recherche).readNullable[RayonRecherche] and
      (JsPath \ numero_telephone).readNullable[NumeroTelephone] and
      (JsPath \ cv_id).readNullable[CVId] and
      (JsPath \ cv_type_media).readNullable[TypeMedia]
    ) (CandidatSaisieCriteresRechercheDocument.apply _)
}