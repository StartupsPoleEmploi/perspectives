package controllers.recruteur

import fr.poleemploi.perspectives.recruteur.alerte.domain.FrequenceAlerte
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

case class LocalisationForm(label: String,
                            latitude: Double,
                            longitude: Double)

case class CreerAlerteForm(frequence: String,
                           secteurActivite: Option[String],
                           metier: Option[String],
                           localisation: Option[LocalisationForm])

object CreerAlerteForm {

  val frequenceAlerteConstraint: Constraint[String] = Constraint("constraint.frequenceAlerte")(
    text => FrequenceAlerte.frequenceAlerte(text)
      .map(_ => Valid)
      .getOrElse(Invalid(Seq(ValidationError("constraint.frequenceAlerte"))))
  )

  def auMoinsUnCritereSelectionne: Constraint[CreerAlerteForm] = Constraint {
    fields =>
      fields.localisation.orElse(fields.secteurActivite).orElse(fields.metier)
        .map(_ => Valid)
        .getOrElse(Invalid(ValidationError("constraint.alerte.selectioncriteres")))
  }


  val form = Form(
    mapping(
      "frequence" -> nonEmptyText.verifying(frequenceAlerteConstraint),
      "secteurActivite" -> optional(text),
      "metier" -> optional(text),
      "localisation" -> optional(mapping(
        "label" -> text,
        "latitude" -> of[Double],
        "longitude" -> of[Double]
      )(LocalisationForm.apply)(LocalisationForm.unapply)),
    )(CreerAlerteForm.apply)(CreerAlerteForm.unapply) verifying auMoinsUnCritereSelectionne
  )
}