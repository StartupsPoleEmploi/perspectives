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

  implicit val format: Format[CandidatCriteresRechercheDocument] = (
    (JsPath \ "metiers_valides").format[Set[CodeROME]] and
      (JsPath \ "metiers").format[Set[CodeROME]] and
      (JsPath \ "domaines_professionnels").format[Set[CodeDomaineProfessionnel]] and
      (JsPath \ "code_postal").formatNullable[String] and
      (JsPath \ "commune").formatNullable[String] and
      (JsPath \ "rayon").formatNullable[RayonRechercheDocument] and
      (JsPath \ "zone").formatNullable[ZoneDocument]
    ) (CandidatCriteresRechercheDocument.apply, unlift(CandidatCriteresRechercheDocument.unapply))
}