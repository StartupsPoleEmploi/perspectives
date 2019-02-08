package controllers.candidat

import fr.poleemploi.perspectives.commun.domain.Coordonnees
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._

case class RechercheOffresForm(metier: Option[String],
                               coordonnees: Option[Coordonnees])

object RechercheOffresForm {

  val form = Form(
    mapping(
      "metier" -> optional(text),
      "coordonnees" -> optional(mapping(
        "latitude" -> of[Double],
        "longitude" -> of[Double]
      )(Coordonnees.apply)(Coordonnees.unapply))
    )(RechercheOffresForm.apply)(RechercheOffresForm.unapply)
  )
}
