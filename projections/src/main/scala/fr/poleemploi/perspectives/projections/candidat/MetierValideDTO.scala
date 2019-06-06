package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, Habilete}
import fr.poleemploi.perspectives.metier.domain.Metier
import play.api.libs.json._

case class MetierValideDTO(metier: Metier,
                           habiletes: Set[Habilete],
                           departement: CodeDepartement,
                           isDHAE: Boolean)

object MetierValideDTO {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[MetierValideDTO] = Json.writes[MetierValideDTO]
}