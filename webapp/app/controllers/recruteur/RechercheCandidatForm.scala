package controllers.recruteur

import play.api.data.Form
import play.api.data.Forms._

case class RechercheCandidatForm(secteurActivite: Option[String],
                                 codeDepartement: Option[String],
                                 metier: Option[String])

object RechercheCandidatForm {

  val form = Form(
    mapping(
      "secteurActivite" -> optional(text),
      "codeDepartement" -> optional(text),
      "metier" -> optional(text)
    )(RechercheCandidatForm.apply)(RechercheCandidatForm.unapply)
  )
}