package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.commun.domain.UniteLongueur
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class RayonRechercheDocument(value: Int, uniteLongueur: UniteLongueur)

object RayonRechercheDocument {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val format: Format[RayonRechercheDocument] = (
    (JsPath \ "value").format[Int] and
      (JsPath \ "unite_longueur").format[UniteLongueur]
    )(RayonRechercheDocument.apply, unlift(RayonRechercheDocument.unapply))
}