package controllers.candidat

import play.api.data.Form
import play.api.data.Forms._

case class RechercheOffresForm(motCle: Option[String],
                               codePostal: Option[String],
                               rayonRecherche: Option[Int],
                               typesContrats: List[String],
                               secteursActivites: List[String],
                               metiers: List[String])

object RechercheOffresForm {

  val form = Form(
    mapping(
      "motCle" -> optional(text),
      "codePostal" -> optional(text),
      "rayonRecherche" -> optional(number),
      "typesContrats" -> list(text),
      "secteursActivites" -> list(text),
      "metiers" -> list(text)
    )(RechercheOffresForm.apply)(RechercheOffresForm.unapply)
  )
}
