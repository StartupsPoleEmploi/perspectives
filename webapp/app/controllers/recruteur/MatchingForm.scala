package controllers.recruteur

import play.api.data.Form
import play.api.data.Forms._

case class MatchingForm(secteurActivite: Option[String],
                        metiers: Set[String])

object MatchingForm {

  val form = Form(
    mapping(
      "secteurActivite" -> optional(text),
      "metiers" -> set(text)
    )(MatchingForm.apply)(MatchingForm.unapply)
  )
}