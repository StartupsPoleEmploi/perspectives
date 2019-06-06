package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import java.time.LocalDate

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class ExperienceProfessionnelleDocument(dateDebut: LocalDate,
                                             dateFin: Option[LocalDate],
                                             enPoste: Boolean,
                                             intitule: String,
                                             nomEntreprise: Option[String],
                                             lieu: Option[String],
                                             description: Option[String])

object ExperienceProfessionnelleDocument {

  implicit val format: Format[ExperienceProfessionnelleDocument] = (
    (JsPath \ "date_debut").format[LocalDate] and
      (JsPath \ "date_fin").formatNullable[LocalDate] and
      (JsPath \ "en_poste").format[Boolean] and
      (JsPath \ "intitule").format[String] and
      (JsPath \ "nom_entreprise").formatNullable[String] and
      (JsPath \ "lieu").formatNullable[String] and
      (JsPath \ "description").formatNullable[String]
    )(ExperienceProfessionnelleDocument.apply, unlift(ExperienceProfessionnelleDocument.unapply))
}
