package controllers.recruteur

import play.api.libs.functional.syntax.{unlift, _}
import play.api.libs.json.{JsPath, Writes}

case class LocalisationDto(label: String,
                           latitude: Double,
                           longitude: Double)

object LocalisationDto {

  implicit val writes: Writes[LocalisationDto] = (
    (JsPath \ "name").write[String] and
      (JsPath \ "latitude").write[Double] and
      (JsPath \ "longitude").write[Double]
    ) (unlift(LocalisationDto.unapply))
}
