package fr.poleemploi.perspectives.rome.infra.elasticsearch

import fr.poleemploi.perspectives.commun.domain.{CodeAppellation, CodeROME}
import play.api.libs.json.{Format, Json}

case class AppellationDocument(codeROME: CodeROME,
                               codeAppellation: CodeAppellation,
                               label: String)

object AppellationDocument {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val format: Format[AppellationDocument] = Json.format[AppellationDocument]
}
