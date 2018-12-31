package fr.poleemploi.perspectives.projections.candidat

import java.time.LocalDateTime

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.{CandidatId, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import play.api.libs.functional.syntax.{unlift, _}
import play.api.libs.json._

case class CandidatsPourConseillerQuery(nbPagesACharger: Int,
                                        page: Option[KeysetCandidatsPourConseiller]) extends Query[CandidatsPourConseillerQueryResult] {
  val nbCandidatsParPage = 20
}

case class CandidatsPourConseillerQueryResult(candidats: List[CandidatPourConseillerDto],
                                              pages: List[KeysetCandidatsPourConseiller],
                                              pageSuivante: Option[KeysetCandidatsPourConseiller]) extends QueryResult

case class CandidatPourConseillerDto(candidatId: CandidatId,
                                     nom: String,
                                     prenom: String,
                                     genre: Genre,
                                     email: Email,
                                     statutDemandeurEmploi: Option[StatutDemandeurEmploi],
                                     rechercheMetierEvalue: Option[Boolean],
                                     metiersEvalues: List[Metier],
                                     rechercheAutreMetier: Option[Boolean],
                                     metiersRecherches: List[Metier],
                                     contacteParAgenceInterim: Option[Boolean],
                                     contacteParOrganismeFormation: Option[Boolean],
                                     rayonRecherche: Option[RayonRecherche],
                                     numeroTelephone: Option[NumeroTelephone],
                                     dateInscription: LocalDateTime,
                                     dateDerniereConnexion: LocalDateTime) {

  /**
    * Ne se base pas sur statutDemandeurEmploi car il n'est pas forcément actualisé tout de suite
    * par le candidat et cela implique une reconnexion du candidat via un service externe.
    */
  val rechercheEmploi: Boolean =
    (rechercheMetierEvalue.isEmpty && rechercheAutreMetier.isEmpty) ||
      rechercheMetierEvalue.getOrElse(false) || rechercheAutreMetier.getOrElse(false)
}

object CandidatPourConseillerDto {

  implicit val writes: Writes[CandidatPourConseillerDto] = (a: CandidatPourConseillerDto) =>
    Json.writes[CandidatPourConseillerDto].writes(a) ++ Json.obj("rechercheEmploi" -> JsBoolean(a.rechercheEmploi))
}

case class KeysetCandidatsPourConseiller(dateInscription: Long,
                                         candidatId: CandidatId)

object KeysetCandidatsPourConseiller {

  implicit val writes: Writes[KeysetCandidatsPourConseiller] = (
    (JsPath \ "dateInscription").write[Long] and
      (JsPath \ "candidatId").write[CandidatId]
    ) (unlift(KeysetCandidatsPourConseiller.unapply))
}