package fr.poleemploi.perspectives.projections.metier

import fr.poleemploi.perspectives.commun.domain.CodeDomaineProfessionnel
import play.api.libs.json.{Json, Writes}

case class DomaineProfessionnelDTO(code: CodeDomaineProfessionnel,
                                   label: String)

object DomaineProfessionnelDTO {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[DomaineProfessionnelDTO] = Json.writes[DomaineProfessionnelDTO]
}
