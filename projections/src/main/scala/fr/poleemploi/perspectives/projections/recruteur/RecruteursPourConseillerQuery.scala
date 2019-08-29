package fr.poleemploi.perspectives.projections.recruteur

import java.time.{LocalDate, ZonedDateTime}

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.recruteur.{NumeroSiret, RecruteurId, TypeRecruteur}
import play.api.libs.json.{Json, Writes}

case class RecruteursPourConseillerQuery(codesDepartement: List[CodeDepartement],
                                         codePostal: Option[String],
                                         dateDebut: Option[LocalDate],
                                         dateFin: Option[LocalDate],
                                         typeRecruteur: Option[TypeRecruteur],
                                         contactParCandidats: Option[Boolean],
                                         page: Option[KeysetRecruteursPourConseiller]) extends Query[RecruteursPourConseillerQueryResult] {
  val nbRecruteursParPage = 20
}

case class RecruteursPourConseillerQueryResult(nbRecruteursTotal: Int,
                                               recruteurs: List[RecruteurPourConseillerDto],
                                               pageSuivante: Option[KeysetRecruteursPourConseiller]) extends QueryResult

object RecruteursPourConseillerQueryResult {

  implicit val writes: Writes[RecruteursPourConseillerQueryResult] = Json.writes[RecruteursPourConseillerQueryResult]
}

case class RecruteurPourConseillerDto(recruteurId: RecruteurId,
                                      nom: Nom,
                                      prenom: Prenom,
                                      email: Email,
                                      typeRecruteur: Option[TypeRecruteur],
                                      raisonSociale: Option[String],
                                      codePostal: Option[String],
                                      commune: Option[String],
                                      contactParCandidats: Option[Boolean],
                                      numeroSiret: Option[NumeroSiret],
                                      numeroTelephone: Option[NumeroTelephone],
                                      dateInscription: ZonedDateTime,
                                      dateDerniereConnexion: ZonedDateTime)

object RecruteurPourConseillerDto {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  def tupled = (RecruteurPourConseillerDto.apply _).tupled

  implicit val writes: Writes[RecruteurPourConseillerDto] = Json.writes[RecruteurPourConseillerDto]
}

/**
  * @param recruteurId sert de tiebreaker au cas où deux recruteurs se seraient inscrits à la même date
  */
case class KeysetRecruteursPourConseiller(dateInscription: ZonedDateTime,
                                          recruteurId: RecruteurId)

object KeysetRecruteursPourConseiller {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[KeysetRecruteursPourConseiller] = Json.writes[KeysetRecruteursPourConseiller]
}
