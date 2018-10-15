package controllers.recruteur

import fr.poleemploi.perspectives.recruteur.alerte.domain.FrequenceAlerte
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

case class CreerAlerteForm(secteurActivite: Option[String],
                           metier: Option[String],
                           codeDepartement: Option[String],
                           frequence: String)

object CreerAlerteForm {

  val frequenceAlerteConstraint: Constraint[String] = Constraint("constraint.frequenceAlerte")(
    text => FrequenceAlerte.frequenceAlerte(text)
      .map(_ => Valid)
      .getOrElse(Invalid(Seq(ValidationError("constraint.frequenceAlerte"))))
  )

  def auMoinsUnCritereSelectionne: Constraint[CreerAlerteForm] = Constraint {
    fields =>
      fields.codeDepartement.orElse(fields.secteurActivite).orElse(fields.metier)
        .map(_ => Valid)
        .getOrElse(Invalid(ValidationError("constraint.alerte.selectioncriteres")))
  }

  val form = Form(
    mapping(
      "secteurActivite" -> optional(text),
      "metier" -> optional(text),
      "codeDepartement" -> optional(text),
      "frequence" -> nonEmptyText.verifying(frequenceAlerteConstraint),
    )(CreerAlerteForm.apply)(CreerAlerteForm.unapply) verifying auMoinsUnCritereSelectionne
  )
}

