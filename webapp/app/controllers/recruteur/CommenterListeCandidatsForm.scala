package controllers.recruteur

import play.api.data.Form
import play.api.data.Forms._

case class CommenterListeCandidatsForm(secteurActiviteRecherche: Option[String],
                                       metierRecherche: Option[String],
                                       localisationRecherche: Option[String],
                                       commentaire: String)

object CommenterListeCandidatsForm {

  val form = Form(
    mapping(
      "secteurActiviteRecherche" -> optional(text),
      "metierRecherche" -> optional(text),
      "localisationRecherche" -> optional(text),
      "commentaire" -> nonEmptyText,
    )(CommenterListeCandidatsForm.apply)(CommenterListeCandidatsForm.unapply)
  )
}
