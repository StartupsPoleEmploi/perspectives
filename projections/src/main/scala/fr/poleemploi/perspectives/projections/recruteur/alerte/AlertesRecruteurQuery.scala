package fr.poleemploi.perspectives.projections.recruteur.alerte

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import fr.poleemploi.perspectives.recruteur.alerte.domain.{AlerteId, FrequenceAlerte, LocalisationAlerte}
import fr.poleemploi.perspectives.recruteur.{RecruteurId, TypeRecruteur}
import play.api.libs.json.{Json, Writes}

case class AlertesRecruteurQuery(recruteurId: RecruteurId) extends Query[AlertesRecruteurQueryResult]

case class AlertesRecruteurQueryResult(alertes: List[AlerteRecruteurDTO]) extends QueryResult

case class AlerteRecruteurDTO(recruteurId: RecruteurId,
                              typeRecruteur: TypeRecruteur,
                              email: Email,
                              alerteId: AlerteId,
                              frequence: FrequenceAlerte,
                              codeSecteurActivite: Option[CodeSecteurActivite],
                              codeROME: Option[CodeROME],
                              localisation: Option[LocalisationAlerte])

object AlerteRecruteurDTO {

  // FIXME : writes utilisé pour le front et DTO utilisé pour les batchs : REUSE NAZE
  implicit val writes: Writes[AlerteRecruteurDTO] = Writes { a =>
    Json.obj(
      "id" -> a.alerteId,
      "frequence" -> a.frequence,
      "secteurActivite" -> a.codeSecteurActivite,
      "codeROME" -> a.codeROME,
      "localisation" -> Json.obj(
        "label" -> a.localisation.map(_.label),
        "latitude" -> a.localisation.map(_.coordonnees.latitude),
        "longitude" -> a.localisation.map(_.coordonnees.longitude)
      )
    )
  }
}