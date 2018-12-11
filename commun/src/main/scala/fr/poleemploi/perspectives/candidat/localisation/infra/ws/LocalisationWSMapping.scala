package fr.poleemploi.perspectives.candidat.localisation.infra.ws

import fr.poleemploi.perspectives.commun.domain.Coordonnees
import play.api.libs.functional.syntax._
import play.api.libs.json._

class LocalisationWSMapping {

  def buildCoordonnees(geometryCoordinates: GeometryCoordinates) =
    Coordonnees(
      latitude = geometryCoordinates.latitude,
      longitude = geometryCoordinates.longitude
    )

}

case class GeometryCoordinates(longitude: Double,
                               latitude: Double)

object GeometryCoordinates {

  implicit val reads: Reads[GeometryCoordinates] = (
    (JsPath \ 0).read[Double] and
      (JsPath \ 1).read[Double]
    ) (GeometryCoordinates.apply _)
}
