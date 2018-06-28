package controllers.recruteur

import fr.poleemploi.perspectives.domain.NumeroTelephone
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

  val numeroTelephoneConstraint: Constraint[String] = Constraint("constraint.numeroTelephone")({
    text =>
      if (NumeroTelephone.from(text).isDefined) {
        Valid
      } else {
        Invalid(Seq(ValidationError("constraint.numeroTelephone")))
      }
  })

  val form = Form(
    mapping(
      "typeRecruteur" -> nonEmptyText,
      "raisonSociale" -> nonEmptyText,
      "numeroSiret" -> nonEmptyText.verifying(numeroSiretConstraint),
      "numeroTelephone" -> nonEmptyText.verifying(numeroTelephoneConstraint),
      "contactParCandidats" -> nonEmptyText
    )(ProfilForm.apply)(ProfilForm.unapply)
  )
}