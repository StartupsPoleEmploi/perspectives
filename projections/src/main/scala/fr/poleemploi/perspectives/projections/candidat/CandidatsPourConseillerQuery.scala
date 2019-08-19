package fr.poleemploi.perspectives.projections.candidat

import java.time.{LocalDate, LocalDateTime}

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.{CandidatId, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.metier.domain.Metier
import play.api.libs.json._

case class CandidatsPourConseillerQuery(codesDepartement: List[CodeDepartement],
                                        codePostal: Option[String],
                                        dateDebut: Option[LocalDate],
                                        dateFin: Option[LocalDate],
                                        codeSecteurActivite: Option[CodeSecteurActivite],
                                        page: Option[KeysetCandidatsPourConseiller]) extends Query[CandidatsPourConseillerQueryResult] {
  val nbCandidatsParPage = 20
}

case class CandidatsPourConseillerQueryResult(nbCandidatsTotal: Int,
                                              candidats: List[CandidatPourConseillerDto],
                                              pageSuivante: Option[KeysetCandidatsPourConseiller]) extends QueryResult

object CandidatsPourConseillerQueryResult {

  implicit val writes: Writes[CandidatsPourConseillerQueryResult] = Json.writes[CandidatsPourConseillerQueryResult]
}

case class CandidatPourConseillerDto(candidatId: CandidatId,
                                     nom: Nom,
                                     prenom: Prenom,
                                     email: Email,
                                     statutDemandeurEmploi: Option[StatutDemandeurEmploi],
                                     metiersValides: Set[MetierValideDTO],
                                     metiersValidesRecherches: Set[Metier],
                                     metiersRecherches: Set[Metier],
                                     contactRecruteur: Option[Boolean],
                                     contactFormation: Option[Boolean],
                                     communeRecherche: Option[String],
                                     codePostalRecherche: Option[String],
                                     rayonRecherche: Option[RayonRecherche],
                                     numeroTelephone: Option[NumeroTelephone],
                                     dateInscription: LocalDateTime,
                                     dateDerniereConnexion: LocalDateTime)

object CandidatPourConseillerDto {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[CandidatPourConseillerDto] = Json.writes[CandidatPourConseillerDto]
}

/**
  * @param candidatId sert de tiebreaker au cas où deux candidats se seraient inscrits à la même date
  */
case class KeysetCandidatsPourConseiller(dateInscription: Long,
                                         candidatId: CandidatId)

object KeysetCandidatsPourConseiller {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[KeysetCandidatsPourConseiller] = Json.writes[KeysetCandidatsPourConseiller]
}