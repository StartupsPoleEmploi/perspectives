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

  /**
    * Utilisé pour transformer un Option[Boolean] en String :
    * si on utilise boolean dans le mapping du form Play le champ sera pré-rempli par faux, ce qu'on ne souhaite pas forcément
    */
  def optBooleanToString(optBoolean: Option[Boolean]): String =
    optBoolean.map(b => if (b) "true" else "false").getOrElse("")

  /**
    * Utilisé pour récupérer un Boolean depuis un String (soumission de formulaire sans passer par le mapping Play boolean)
    */
  def stringToBoolean(string: String): Boolean = if ("true".equalsIgnoreCase(string)) true else false
}
