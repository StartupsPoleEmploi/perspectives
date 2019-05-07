package fr.poleemploi.perspectives.projections.candidat

import java.time.LocalDateTime

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.{CandidatId, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import fr.poleemploi.perspectives.metier.domain.Metier
import play.api.libs.json._

case class CandidatsPourConseillerQuery(nbPagesACharger: Int,
                                        page: Option[KeysetCandidatsPourConseiller]) extends Query[CandidatsPourConseillerQueryResult] {
  val nbCandidatsParPage = 20
}

case class CandidatsPourConseillerQueryResult(candidats: List[CandidatPourConseillerDto],
                                              pages: List[KeysetCandidatsPourConseiller],
                                              pageSuivante: Option[KeysetCandidatsPourConseiller]) extends QueryResult

case class MetierValideDto(metier: Metier,
                           departement: CodeDepartement,
                           isDHAE: Boolean)

object MetierValideDto {

  implicit val writes: Writes[MetierValideDto] = Json.writes[MetierValideDto]
}

case class CandidatPourConseillerDto(candidatId: CandidatId,
                                     nom: Nom,
                                     prenom: Prenom,
                                     email: Email,
                                     statutDemandeurEmploi: Option[StatutDemandeurEmploi],
                                     metiersValides: Set[MetierValideDto],
                                     metiersValidesRecherches: Set[Metier],
                                     metiersRecherches: Set[Metier],
                                     contactRecruteur: Option[Boolean],
                                     contactFormation: Option[Boolean],
                                     communeRecherche: Option[String],
                                     codePostalRecherche: Option[String],
                                     rayonRecherche: Option[RayonRecherche],
                                     numeroTelephone: Option[NumeroTelephone],
                                     dateInscription: LocalDateTime,
                                     dateDerniereConnexion: LocalDateTime) {

  /**
    * Ne se base pas sur statutDemandeurEmploi car il n'est pas forcément actualisé tout de suite
    * par le candidat et cela implique une reconnexion via un service externe.
    */
  val rechercheEmploi: Boolean =
    metiersValidesRecherches.nonEmpty ||
      metiersRecherches.nonEmpty
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