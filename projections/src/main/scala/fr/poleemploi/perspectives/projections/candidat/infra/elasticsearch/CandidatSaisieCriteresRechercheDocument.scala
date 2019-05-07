package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.NumeroTelephone
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CandidatSaisieCriteresRechercheDocument(candidatId: CandidatId,
                                                   contactRecruteur: Option[Boolean],
                                                   contactFormation: Option[Boolean],
                                                   metiersValides: Set[MetierValideDocument],
                                                   commune: Option[String],
                                                   codePostal: Option[String],
                                                   latitude: Option[Double],
                                                   longitude: Option[Double],
                                                   numeroTelephone: Option[NumeroTelephone],
                                                   criteresRecherche: CandidatCriteresRechercheDocument)

object CandidatSaisieCriteresRechercheDocument {

  import CandidatProjectionElasticsearchMapping._
  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val reads: Reads[CandidatSaisieCriteresRechercheDocument] = (
    (JsPath \ candidat_id).read[CandidatId] and
      (JsPath \ contact_recruteur).readNullable[Boolean] and
      (JsPath \ contact_formation).readNullable[Boolean] and
      (JsPath \ metiers_valides).read[Set[MetierValideDocument]] and
      (JsPath \ commune).readNullable[String] and
      (JsPath \ code_postal).readNullable[String] and
      (JsPath \ latitude).readNullable[Double] and
      (JsPath \ longitude).readNullable[Double] and
      (JsPath \ numero_telephone).readNullable[NumeroTelephone] and
      (JsPath \ criteres_recherche).read[CandidatCriteresRechercheDocument]
    ) (CandidatSaisieCriteresRechercheDocument.apply _)
}