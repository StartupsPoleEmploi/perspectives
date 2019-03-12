package fr.poleemploi.perspectives.projections.metier

import fr.poleemploi.perspectives.commun.domain.CodeROME
import play.api.libs.json.Json
import play.api.libs.json._

// FIXME : supprimer au final?
case class MetierDTO(codeROME: CodeROME,
                     label: String)

object MetierDTO {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[MetierDTO] = Json.writes[MetierDTO]
}
