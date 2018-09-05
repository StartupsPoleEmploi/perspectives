package fr.poleemploi.perspectives.metier.infra.file

import fr.poleemploi.perspectives.commun.domain.CodeROME
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

private[file] case class MetierFileDto(codeROME: CodeROME, label: String)

object MetierFileDto {

  implicit val metierFileDtoReads: Reads[MetierFileDto] = (
    (JsPath \ "codeROME").read[String].map(CodeROME) and
      (JsPath \ "label").read[String]
    ) (MetierFileDto.apply _)
}