package controllers

import fr.poleemploi.perspectives.commun.domain.NumeroTelephone
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

object FormHelpers {

  val numeroTelephoneConstraint: Constraint[String] = Constraint("constraint.numeroTelephone")({
    text =>
      if (NumeroTelephone.from(text).isDefined) {
        Valid
      } else {
        Invalid(Seq(ValidationError("constraint.numeroTelephone")))
      }
  })

  def booleanToString(boolean: Boolean): String = if (boolean) "true" else "false"
}
