package fr.poleemploi.perspectives.projections.recruteur.alerte

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

// FIXME : typage
case class Criteres(codeSecteurActivite: String,
                    codeROME: String,
                    codeDepartement: String)

object Criteres {

  implicit val criteresWrites: Writes[Criteres] = (
    (JsPath \ "codeSecteurActivite").write[String] and
      (JsPath \ "codeROME").write[String] and
      (JsPath \ "codeDepartement").write[String]
    ) (unlift(Criteres.unapply))
}

case class AlerteDto(alerteId: String,
                     intitule: String,
                     frequence: String,
                     criteres: Criteres)

object AlerteDto {

  implicit val alerteWrites: Writes[AlerteDto] = (
    (JsPath \ "id").write[String] and
      (JsPath \ "intitule").write[String] and
      (JsPath \ "frequence").write[String] and
      (JsPath \ "criteres").write[Criteres]
    ) (unlift(AlerteDto.unapply))
}
