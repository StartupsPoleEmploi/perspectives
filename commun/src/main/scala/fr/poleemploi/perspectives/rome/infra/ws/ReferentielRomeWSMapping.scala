package fr.poleemploi.perspectives.rome.infra.ws

import fr.poleemploi.perspectives.commun.domain.{CodeAppellation, CodeROME}
import fr.poleemploi.perspectives.rome.domain.Appellation
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

class ReferentielRomeWSMapping {

  def buildAppellation(codeROME: CodeROME, response: AppellationResponse): Appellation =
    Appellation(
      codeROME = codeROME,
      codeAppellation = CodeAppellation(response.code),
      label = response.libelle
    )
}

case class AppellationResponse(code: String,
                               libelle: String,
                               libelleCourt: String,
                               particulier: Boolean)

object AppellationResponse {

  implicit val reads: Reads[AppellationResponse] = (
    (JsPath \ "code").read[String] and
      (JsPath \ "libelle").read[String] and
      (JsPath \ "libelleCourt").read[String] and
      (JsPath \ "particulier").read[Boolean]
    ) (AppellationResponse.apply _)
}
