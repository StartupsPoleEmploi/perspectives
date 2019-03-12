package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.commun.domain.{Nom, Prenom}
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CandidatDepotCVDocument(candidatId: CandidatId,
                                   nom: Nom,
                                   prenom: Prenom,
                                   cvId: Option[CVId],
                                   cvTypeMedia: Option[TypeMedia])

object CandidatDepotCVDocument {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
  import CandidatProjectionElasticsearchMapping._

  implicit val reads: Reads[CandidatDepotCVDocument] = (
    (JsPath \ candidat_id).read[CandidatId] and
      (JsPath \ nom).read[Nom] and
      (JsPath \ prenom).read[Prenom] and
      (JsPath \ cv_id).readNullable[CVId] and
      (JsPath \ cv_type_media).readNullable[TypeMedia]
    ) (CandidatDepotCVDocument.apply _)
}


