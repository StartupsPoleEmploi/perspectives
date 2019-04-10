package fr.poleemploi.perspectives.metier.domain

import fr.poleemploi.perspectives.commun.domain.CodeROME
import play.api.libs.json.{Format, Json}

case class Metier(codeROME: CodeROME,
                  label: String)

object Metier {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val format: Format[Metier] = Json.format[Metier]
}
