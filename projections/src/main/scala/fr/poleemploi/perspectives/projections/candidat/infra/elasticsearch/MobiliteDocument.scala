package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class MobiliteDocument(typeMobilite: String,
                            longitude: Double,
                            latitude: Double,
                            radius: Option[String])

object MobiliteDocument {

  implicit val reads: Reads[MobiliteDocument] = (
    (JsPath \ "type").read[String] and
      (JsPath \ "coordinates" \ 0).read[Double] and
      (JsPath \ "coordinates" \ 1).read[Double] and
      (JsPath \ "radius").readNullable[String]
    ) (MobiliteDocument.apply _)

  implicit val writes: Writes[MobiliteDocument] = Writes { mobilite =>
    mobilite.radius.map(radius =>
      Json.obj(
        "type" -> s"${mobilite.typeMobilite}",
        "coordinates" -> JsArray(Seq(JsNumber(mobilite.longitude), JsNumber(mobilite.latitude))),
        "radius" -> s"${radius}km"
      )
    ).getOrElse(
      Json.obj(
        "type" -> s"${mobilite.typeMobilite}",
        "coordinates" -> JsArray(Seq(JsNumber(mobilite.longitude), JsNumber(mobilite.latitude)))
      )
    )
  }

}