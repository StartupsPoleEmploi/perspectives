package controllers

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

case class SaisieCriteresRechercheForm(rechercheMetierEvalue: String,
                                       rechercheAutreMetier: String,
                                       metiersRecherches: Set[String],
                                       etreContacteParOrganismeFormation: String,
                                       etreContacteParAgenceInterim: String,
                                       rayonRecherche: Int)

object SaisieCriteresRechercheForm {

  val form = Form(
    mapping(
      "rechercheMetierEvalue" -> nonEmptyText,
      "rechercheAutreMetier" -> nonEmptyText,
      "listeMetiersRecherches" -> set(nonEmptyText),
      "contactFormation" -> nonEmptyText,
      "contactInterim" -> nonEmptyText,
      "rayonRecherche" -> number
    )(SaisieCriteresRechercheForm.apply)(SaisieCriteresRechercheForm.unapply) verifying metiersSelectionnes
  )

  def metiersSelectionnes: Constraint[SaisieCriteresRechercheForm] =
    Constraint {
      fields =>
        if (fields.rechercheAutreMetier == "true" && fields.metiersRecherches.isEmpty) {
          Invalid(ValidationError("constraint.criteres.selectionmetiers"))
        } else {
          Valid
        }
    }
}