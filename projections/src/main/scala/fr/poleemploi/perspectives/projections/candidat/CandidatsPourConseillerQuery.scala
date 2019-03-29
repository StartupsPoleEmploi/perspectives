package fr.poleemploi.perspectives.projections.candidat

import java.time.LocalDateTime

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.{CandidatId, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import play.api.libs.json._

case class CandidatsPourConseillerQuery(nbPagesACharger: Int,
                                        page: Option[KeysetCandidatsPourConseiller]) extends Query[CandidatsPourConseillerQueryResult] {
  val nbCandidatsParPage = 20
}

case class CandidatsPourConseillerQueryResult(candidats: List[CandidatPourConseillerDto],
                                              pages: List[KeysetCandidatsPourConseiller],
                                              pageSuivante: Option[KeysetCandidatsPourConseiller]) extends QueryResult

case class CandidatPourConseillerDto(candidatId: CandidatId,
                                     nom: Nom,
                                     prenom: Prenom,
                                     genre: Genre,
                                     email: Email,
                                     statutDemandeurEmploi: Option[StatutDemandeurEmploi],
                                     rechercheMetiersEvalues: Option[Boolean],
                                     metiersEvalues: List[Metier],
                                     rechercheAutresMetiers: Option[Boolean],
                                     metiersRecherches: List[Metier],
                                     contacteParAgenceInterim: Option[Boolean],
                                     contacteParOrganismeFormation: Option[Boolean],
                                     commune: Option[String],
                                     codePostal: Option[String],
                                     rayonRecherche: Option[RayonRecherche],
                                     numeroTelephone: Option[NumeroTelephone],
                                     dateInscription: LocalDateTime,
                                     dateDerniereConnexion: LocalDateTime) {

  /**
    * Ne se base pas sur statutDemandeurEmploi car il n'est pas forcément actualisé tout de suite
    * par le candidat et cela implique une reconnexion du candidat via un service externe.
    */
  val rechercheEmploi: Boolean =
    (rechercheMetiersEvalues.isEmpty && rechercheAutresMetiers.isEmpty) ||
      rechercheMetiersEvalues.getOrElse(false) || rechercheAutresMetiers.getOrElse(false)
}

object CandidatPourConseillerDto {

  implicit val writes: Writes[CandidatPourConseillerDto] = (a: CandidatPourConseillerDto) =>
    Json.writes[CandidatPourConseillerDto].writes(a) ++ Json.obj("rechercheEmploi" -> JsBoolean(a.rechercheEmploi))
}

case class KeysetCandidatsPourConseiller(dateInscription: Long,
                                         candidatId: CandidatId)

object KeysetCandidatsPourConseiller {

  implicit val writes: Writes[KeysetCandidatsPourConseiller] = Json.writes[KeysetCandidatsPourConseiller]
}