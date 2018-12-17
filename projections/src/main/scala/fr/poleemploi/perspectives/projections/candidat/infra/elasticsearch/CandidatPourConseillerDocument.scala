package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import java.time.LocalDateTime

import fr.poleemploi.perspectives.candidat.{CandidatId, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CandidatPourConseillerDocument(candidatId: CandidatId,
                                          nom: String,
                                          prenom: String,
                                          genre: Genre,
                                          email: Email,
                                          statutDemandeurEmploi: Option[StatutDemandeurEmploi],
                                          rechercheMetierEvalue: Option[Boolean],
                                          metiersEvalues: List[CodeROME],
                                          rechercheAutreMetier: Option[Boolean],
                                          metiersRecherches: List[CodeROME],
                                          contacteParAgenceInterim: Option[Boolean],
                                          contacteParOrganismeFormation: Option[Boolean],
                                          rayonRecherche: Option[RayonRecherche],
                                          numeroTelephone: Option[NumeroTelephone],
                                          dateInscription: LocalDateTime,
                                          dateDerniereConnexion: LocalDateTime)

object CandidatPourConseillerDocument {

  import CandidatProjectionElasticsearchEsMapping._

  implicit val reads: Reads[CandidatPourConseillerDocument] = (
    (JsPath \ candidat_id).read[CandidatId] and
      (JsPath \ nom).read[String] and
      (JsPath \ prenom).read[String] and
      (JsPath \ genre).read[Genre] and
      (JsPath \ email).read[Email] and
      (JsPath \ statut_demandeur_emploi).readNullable[StatutDemandeurEmploi] and
      (JsPath \ recherche_metiers_evalues).readNullable[Boolean] and
      (JsPath \ metiers_evalues).read[List[CodeROME]] and
      (JsPath \ recherche_autres_metiers).readNullable[Boolean] and
      (JsPath \ metiers_recherches).read[List[CodeROME]] and
      (JsPath \ contacte_par_agence_interim).readNullable[Boolean] and
      (JsPath \ contacte_par_organisme_formation).readNullable[Boolean] and
      (JsPath \ rayon_recherche).readNullable[RayonRecherche] and
      (JsPath \ numero_telephone).readNullable[NumeroTelephone] and
      (JsPath \ date_inscription).read[LocalDateTime] and
      (JsPath \ date_derniere_connexion).read[LocalDateTime]
    ) (CandidatPourConseillerDocument.apply _)
}
