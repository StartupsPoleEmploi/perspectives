package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import java.time.LocalDate

import fr.poleemploi.perspectives.candidat._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CandidatSaisieDisponibilitesDocument(candidatId: CandidatId,
                                                contactRecruteur: Option[Boolean],
                                                contactFormation: Option[Boolean],
                                                dateProchaineDisponibilite: Option[LocalDate],
                                                emploiTrouveGracePerspectives: Option[Boolean])

object CandidatSaisieDisponibilitesDocument {

  import CandidatProjectionElasticsearchMapping._
  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val reads: Reads[CandidatSaisieDisponibilitesDocument] = (
    (JsPath \ candidat_id).read[CandidatId] and
      (JsPath \ contact_recruteur).readNullable[Boolean] and
      (JsPath \ contact_formation).readNullable[Boolean] and
      (JsPath \ prochaine_disponibilite).readNullable[LocalDate] and
      (JsPath \ emploi_trouve_grace_perspectives).readNullable[Boolean]
    ) (CandidatSaisieDisponibilitesDocument.apply _)
}
