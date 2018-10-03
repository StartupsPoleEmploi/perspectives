package controllers.recruteur

import play.api.data.Form
import play.api.data.Forms._

case class CommenterListeCandidatsForm(secteurActiviteRecherche: Option[String],
                                       metierRecherche: Option[String],
                                       departementRecherche: Option[String],
                                       commentaire: String)

object CommenterListeCandidatsForm {

  val form = Form(
    mapping(
      "secteurActiviteRecherche" -> optional(text),
      "metierRecherche" -> optional(text),
      "departementRecherche" -> optional(text),
      "commentaire" -> nonEmptyText,
    )(CommenterListeCandidatsForm.apply)(CommenterListeCandidatsForm.unapply)
  )
}
