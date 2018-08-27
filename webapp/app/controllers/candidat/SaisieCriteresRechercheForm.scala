package controllers.candidat

import controllers.FormHelpers
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

case class SaisieCriteresRechercheForm(nouveauCandidat: Boolean,
                                       numeroTelephone: String,
                                       rechercheMetierEvalue: String,
                                       rechercheAutreMetier: String,
                                       metiersRecherches: Set[String],
                                       etreContacteParOrganismeFormation: String,
                                       etreContacteParAgenceInterim: String,
                                       rayonRecherche: Int)

object SaisieCriteresRechercheForm {

  val mediaTypesValides = List(
    "application/pdf",
    "application/vnd.oasis.opendocument.text",
    "image/jpeg",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
  )

  val form = Form(
    mapping(
      "nouveauCandidat" -> boolean,
      "numeroTelephone" -> nonEmptyText.verifying(FormHelpers.numeroTelephoneConstraint),
      "rechercheMetierEvalue" -> nonEmptyText,
      "rechercheAutreMetier" -> nonEmptyText,
      "listeMetiersRecherches" -> set(nonEmptyText),
      "contactFormation" -> nonEmptyText,
      "contactInterim" -> nonEmptyText,
      "rayonRecherche" -> number
    )(SaisieCriteresRechercheForm.apply)(SaisieCriteresRechercheForm.unapply) verifying metiersSelectionnes
  )

  val nouveauCandidat: Form[SaisieCriteresRechercheForm] = SaisieCriteresRechercheForm.form.fill(
    SaisieCriteresRechercheForm(
      nouveauCandidat = true,
      rechercheMetierEvalue = "",
      rechercheAutreMetier = "",
      metiersRecherches = Set.empty,
      etreContacteParAgenceInterim = "",
      etreContacteParOrganismeFormation = "",
      rayonRecherche = 0,
      numeroTelephone = ""
    )
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