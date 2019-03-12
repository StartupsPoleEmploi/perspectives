package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.commun.domain.{CodeDomaineProfessionnel, CodeROME}
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class CandidatCriteresRechercheDocument(metiersValides: Set[CodeROME],
                                             metiers: Set[CodeROME],
                                             domainesProfessionels: Set[CodeDomaineProfessionnel],
                                             codePostal: Option[String],
                                             commune: Option[String],
                                             rayon: Option[RayonRechercheDocument],
                                             zone: Option[ZoneDocument])

object CandidatCriteresRechercheDocument {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val reads: Reads[CandidatCriteresRechercheDocument] = (
    (JsPath \ "metiers_valides").read[Set[CodeROME]] and
      (JsPath \ "metiers").read[Set[CodeROME]] and
      (JsPath \ "domaines_professionnels").read[Set[CodeDomaineProfessionnel]] and
      (JsPath \ "code_postal").readNullable[String] and
      (JsPath \ "commune").readNullable[String] and
      (JsPath \ "rayon").readNullable[RayonRechercheDocument] and
      (JsPath \ "zone").readNullable[ZoneDocument]
    ) (CandidatCriteresRechercheDocument.apply _)

  implicit val writes: Writes[CandidatCriteresRechercheDocument] = (
    (JsPath \ "metiers_valides").write[Set[CodeROME]] and
      (JsPath \ "metiers").write[Set[CodeROME]] and
      (JsPath \ "domaines_professionnels").write[Set[CodeDomaineProfessionnel]] and
      (JsPath \ "code_postal").writeNullable[String] and
      (JsPath \ "commune").writeNullable[String] and
      (JsPath \ "rayon").writeNullable[RayonRechercheDocument] and
      (JsPath \ "zone").writeNullable[ZoneDocument]
    ) (unlift(CandidatCriteresRechercheDocument.unapply))
}

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

  implicit val writes: Writes[ZoneDocument] = Writes(mobilite =>
    mobilite.radius.map(radius =>
      Json.obj(
        "type" -> s"${mobilite.typeMobilite}",
        "coordinates" -> JsArray(Seq(JsNumber(mobilite.longitude), JsNumber(mobilite.latitude))),
        "radius" -> s"${radius}km" //FIXME : unite
      )
    ).getOrElse(
      Json.obj(
        "type" -> s"${mobilite.typeMobilite}",
        "coordinates" -> JsArray(Seq(JsNumber(mobilite.longitude), JsNumber(mobilite.latitude)))
      )
    )
  )

}
