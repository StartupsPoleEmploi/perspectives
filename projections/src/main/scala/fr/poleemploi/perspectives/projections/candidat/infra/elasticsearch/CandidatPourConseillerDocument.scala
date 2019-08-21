package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import java.time.LocalDateTime

import fr.poleemploi.perspectives.candidat.{CandidatId, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.domain._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class KeysetCandidatPourConseillerDocument(dateInscription: Long,
                                                candidatId: CandidatId)

object KeysetCandidatPourConseillerDocument {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[KeysetCandidatPourConseillerDocument] =
    k => Json.arr(k.dateInscription, k.candidatId)
}

case class CandidatPourConseillerDocument(candidatId: CandidatId,
                                          nom: Nom,
                                          prenom: Prenom,
                                          email: Email,
                                          statutDemandeurEmploi: Option[StatutDemandeurEmploi],
                                          metiersValides: Set[MetierValideDocument],
                                          metiersValidesRecherches: Set[CodeROME],
                                          metiersRecherches: Set[CodeROME],
                                          contactRecruteur: Option[Boolean],
                                          contactFormation: Option[Boolean],
                                          communeRecherche: Option[String],
                                          codePostalRecherche: Option[String],
                                          rayonRecherche: Option[RayonRechercheDocument],
                                          numeroTelephone: Option[NumeroTelephone],
                                          dateInscription: LocalDateTime,
                                          dateDerniereConnexion: LocalDateTime)

object CandidatPourConseillerDocument {

  import CandidatProjectionElasticsearchMapping._
  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val reads: Reads[CandidatPourConseillerDocument] = (
    (JsPath \ candidat_id).read[CandidatId] and
      (JsPath \ nom).read[Nom] and
      (JsPath \ prenom).read[Prenom] and
      (JsPath \ email).read[Email] and
      (JsPath \ statut_demandeur_emploi).readNullable[StatutDemandeurEmploi] and
      (JsPath \ metiers_valides).read[Set[MetierValideDocument]] and
      (JsPath \ criteres_recherche \ metiers_valides).read[Set[CodeROME]] and
      (JsPath \ criteres_recherche \ "metiers").read[Set[CodeROME]] and
      (JsPath \ contact_recruteur).readNullable[Boolean] and
      (JsPath \ contact_formation).readNullable[Boolean] and
      (JsPath \ criteres_recherche \ commune).readNullable[String] and
      (JsPath \ criteres_recherche \ code_postal).readNullable[String] and
      (JsPath \ criteres_recherche \ "rayon").readNullable[RayonRechercheDocument] and
      (JsPath \ numero_telephone).readNullable[NumeroTelephone] and
      (JsPath \ date_inscription).read[LocalDateTime] and
      (JsPath \ date_derniere_connexion).read[LocalDateTime]
    ) (CandidatPourConseillerDocument.apply _)
}
