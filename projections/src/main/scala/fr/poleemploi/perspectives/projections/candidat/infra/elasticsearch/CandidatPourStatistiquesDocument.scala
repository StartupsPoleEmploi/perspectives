package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class CandidatPourStatistiquesDocument(candidatId: CandidatId,
                                            peConnectId: Option[PEConnectId],
                                            identifiantLocal: Option[IdentifiantLocal],
                                            codeNeptune: Option[CodeNeptune],
                                            nom: Nom,
                                            prenom: Prenom,
                                            email: Email,
                                            genre: Genre,
                                            metiersValides: List[MetierValideDocument])

object CandidatPourStatistiquesDocument {

  import CandidatProjectionElasticsearchMapping._
  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val reads: Reads[CandidatPourStatistiquesDocument] = (
    (JsPath \ candidat_id).read[CandidatId] and
      (JsPath \ peconnect_id).readNullable[PEConnectId] and
      (JsPath \ identifiant_local).readNullable[IdentifiantLocal] and
      (JsPath \ code_neptune).readNullable[CodeNeptune] and
      (JsPath \ nom).read[Nom] and
      (JsPath \ prenom).read[Prenom] and
      (JsPath \ email).read[Email] and
      (JsPath \ genre).read[Genre] and
      (JsPath \ metiers_valides).read[List[MetierValideDocument]]
    ) (CandidatPourStatistiquesDocument.apply _)
}
