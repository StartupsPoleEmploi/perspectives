package fr.poleemploi.perspectives.projections.recruteur.alerte

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.recruteur.alerte.domain.{AlerteId, FrequenceAlerte}
import fr.poleemploi.perspectives.recruteur.{RecruteurId, TypeRecruteur}
import play.api.libs.json.{Json, Writes}

case class AlertesRecruteurQuery(recruteurId: RecruteurId) extends Query[AlertesRecruteurQueryResult]

case class AlertesRecruteurQueryResult(alertes: List[AlerteRecruteurDto]) extends QueryResult

case class AlerteRecruteurDto(recruteurId: RecruteurId,
                              typeRecruteur: TypeRecruteur,
                              email: Email,
                              alerteId: AlerteId,
                              frequence: FrequenceAlerte,
                              secteurActivite: Option[SecteurActivite],
                              metier: Option[Metier],
                              localisation: Option[Localisation])

object AlerteRecruteurDto {

  implicit val writes: Writes[AlerteRecruteurDto] = Writes { a =>
    Json.obj(
      "id" -> a.alerteId.value,
      "frequence" -> a.frequence.value,
      "secteurActivite" -> a.secteurActivite.map(_.code.value),
      "metier" -> a.metier.map(_.codeROME.value),
      "localisation" -> Json.obj(
        "label" -> a.localisation.map(_.label),
        "latitude" -> a.localisation.map(_.coordonnees.latitude),
        "longitude" -> a.localisation.map(_.coordonnees.longitude)
      )
    )
  }
}