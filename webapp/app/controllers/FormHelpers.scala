package controllers

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import fr.poleemploi.perspectives.commun.domain.NumeroTelephone
import play.api.data.FormError
import play.api.data.format.Formats._
import play.api.data.format.Formatter
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

object FormHelpers {

  val numeroTelephoneConstraint: Constraint[String] = Constraint("constraint.numeroTelephone")(
    text => NumeroTelephone.from(text)
      .map(_ => Valid)
      .getOrElse(Invalid(Seq(ValidationError("constraint.numeroTelephone"))))
  )

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

  implicit object ZonedDateTimeFormatter extends Formatter[ZonedDateTime] {

    override val format = Some(("format.zonedatetime", Nil))

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], ZonedDateTime] =
      parsing(
        parse = s => ZonedDateTime.parse(s),
        errMsg = "error.zonedatetime",
        errArgs = Nil
      )(key, data)

    override def unbind(key: String, value: ZonedDateTime): Map[String, String] =
      Map(key -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value))
  }
}
