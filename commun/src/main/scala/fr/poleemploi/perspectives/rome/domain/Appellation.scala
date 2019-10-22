package fr.poleemploi.perspectives.rome.domain

import fr.poleemploi.perspectives.commun.domain.{CodeAppellation, CodeROME}
import play.api.libs.json.{Format, Json}

case class Appellation(codeROME: CodeROME,
                       codeAppellation: CodeAppellation,
                       label: String)

object Appellation {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val format: Format[Appellation] = Json.format[Appellation]
}

