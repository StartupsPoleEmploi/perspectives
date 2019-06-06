package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.candidat.{DomaineFormation, NiveauFormation}
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class FormationDocument(anneeFin: Int,
                             intitule: String,
                             lieu: Option[String],
                             domaine: Option[DomaineFormation],
                             niveau: Option[NiveauFormation])

object FormationDocument {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val format: Format[FormationDocument] = (
    (JsPath \ "annee_fin").format[Int] and
      (JsPath \ "intitule").format[String] and
      (JsPath \ "lieu").formatNullable[String] and
      (JsPath \ "domaine").formatNullable[DomaineFormation] and
      (JsPath \ "niveau").formatNullable[NiveauFormation]
    )(FormationDocument.apply, unlift(FormationDocument.unapply))
}
