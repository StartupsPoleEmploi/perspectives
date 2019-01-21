package fr.poleemploi.perspectives.projections.rechercheCandidat

import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import fr.poleemploi.perspectives.commun.domain.CodeSecteurActivite
import play.api.libs.json.{Json, Writes}

case class SecteurActiviteDto(code: CodeSecteurActivite,
                              label: String)

object SecteurActiviteDto {

  implicit val writes: Writes[SecteurActiviteDto] = Json.writes[SecteurActiviteDto]
}