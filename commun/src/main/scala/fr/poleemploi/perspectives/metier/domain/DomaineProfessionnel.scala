package fr.poleemploi.perspectives.metier.domain

import fr.poleemploi.perspectives.commun.domain.CodeDomaineProfessionnel
import play.api.libs.json.{Format, Json}

case class DomaineProfessionnel(code: CodeDomaineProfessionnel,
                                label: String)

object DomaineProfessionnel {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val format: Format[DomaineProfessionnel] = Json.format[DomaineProfessionnel]
}
