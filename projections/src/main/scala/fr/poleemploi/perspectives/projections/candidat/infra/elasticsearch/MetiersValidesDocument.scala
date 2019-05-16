package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, Habilete}
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class MetierValideDocument(metier: CodeROME,
                                departement: CodeDepartement,
                                isDHAE: Boolean)

object MetierValideDocument {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val format: Format[MetierValideDocument] = (
    (JsPath \ "metier").format[CodeROME] and
    (JsPath \ "departement").format[CodeDepartement] and
      (JsPath \ "is_dhae").format[Boolean]
    )(MetierValideDocument.apply, unlift(MetierValideDocument.unapply))
}

case class MetiersValidesDocument(metiersValides: Set[MetierValideDocument],
                                  habiletes: Set[Habilete])

object MetiersValidesDocument {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
  import CandidatProjectionElasticsearchMapping._

  implicit val format: Format[MetiersValidesDocument] = (
    (JsPath \ metiers_valides).format[Set[MetierValideDocument]] and
      (JsPath \ habiletes).format[Set[Habilete]]
    )(MetiersValidesDocument.apply, unlift(MetiersValidesDocument.unapply))
}