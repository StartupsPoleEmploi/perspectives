package fr.poleemploi.perspectives.projections.recruteur

import java.time.ZonedDateTime

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.commun.domain.{Email, Genre, NumeroTelephone}
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import fr.poleemploi.perspectives.recruteur.{NumeroSiret, RecruteurId, TypeRecruteur}
import play.api.libs.functional.syntax.{unlift, _}
import play.api.libs.json.{JsPath, Writes}

case class RecruteursPourConseillerQuery(nbPagesACharger: Int,
                                         page: Option[KeysetRecruteursPourConseiller]) extends Query[RecruteursPourConseillerQueryResult] {
  val nbRecruteursParPage = 20
}

case class RecruteursPourConseillerQueryResult(recruteurs: List[RecruteurPourConseillerDto],
                                               pages: List[KeysetRecruteursPourConseiller],
                                               pageSuivante: Option[KeysetRecruteursPourConseiller]) extends QueryResult

case class RecruteurPourConseillerDto(recruteurId: RecruteurId,
                                      nom: String,
                                      prenom: String,
                                      email: Email,
                                      genre: Genre,
                                      typeRecruteur: Option[TypeRecruteur],
                                      raisonSociale: Option[String],
                                      numeroSiret: Option[NumeroSiret],
                                      numeroTelephone: Option[NumeroTelephone],
                                      contactParCandidats: Option[Boolean],
                                      dateInscription: ZonedDateTime,
                                      dateDerniereConnexion: ZonedDateTime)

case class KeysetRecruteursPourConseiller(dateInscription: ZonedDateTime,
                                          recruteurId: RecruteurId)

object KeysetRecruteursPourConseiller {

  implicit val writes: Writes[KeysetRecruteursPourConseiller] = (
    (JsPath \ "dateInscription").write[ZonedDateTime] and
      (JsPath \ "recruteurId").write[RecruteurId]
    ) (unlift(KeysetRecruteursPourConseiller.unapply))
}
