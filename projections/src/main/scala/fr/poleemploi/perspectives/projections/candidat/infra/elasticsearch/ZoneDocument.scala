package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class ZoneDocument(typeMobilite: String,
                        longitude: Double,
                        latitude: Double,
                        radius: Option[String])

object ZoneDocument {

  implicit val reads: Reads[ZoneDocument] = (
    (JsPath \ "type").read[String] and
      (JsPath \ "coordinates" \ 0).read[Double] and
      (JsPath \ "coordinates" \ 1).read[Double] and
      (JsPath \ "radius").readNullable[String]
    ) (ZoneDocument.apply _)

  implicit val writes: Writes[ZoneDocument] = Writes(z =>
    Json.obj(
      "type" -> s"${z.typeMobilite}",
      "coordinates" -> JsArray(Seq(JsNumber(z.longitude), JsNumber(z.latitude)))
    ) ++ z.radius
      .map(r => Json.obj("radius" -> r))
      .getOrElse(Json.obj())
  )
}