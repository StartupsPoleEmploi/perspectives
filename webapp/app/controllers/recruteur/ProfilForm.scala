package controllers.recruteur

import controllers.FormHelpers
import fr.poleemploi.perspectives.domain.recruteur.NumeroSiret
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

case class ProfilForm(typeRecruteur: String,
                      raisonSociale: String,
                      numeroSiret: String,
                      numeroTelephone: String,
                      contactParCandidats: String)

object ProfilForm {


  val numeroSiretConstraint: Constraint[String] = Constraint("constraint.numeroSiret")({
    text =>
      if (NumeroSiret.from(text).isDefined) {
        Valid
      } else {
        Invalid(Seq(ValidationError("constraint.numeroSiret")))
      }
  })

  val form = Form(
    mapping(
      "typeRecruteur" -> nonEmptyText,
      "raisonSociale" -> nonEmptyText,
      "numeroSiret" -> nonEmptyText.verifying(numeroSiretConstraint),
      "numeroTelephone" -> nonEmptyText.verifying(FormHelpers.numeroTelephoneConstraint),
      "contactParCandidats" -> nonEmptyText
    )(ProfilForm.apply)(ProfilForm.unapply)
  )

  val emptyForm: Form[ProfilForm] = ProfilForm.form.fill(
    ProfilForm(
      typeRecruteur = "",
      raisonSociale = "",
      numeroSiret = "",
      numeroTelephone = "",
      contactParCandidats = ""
    )
  )
}