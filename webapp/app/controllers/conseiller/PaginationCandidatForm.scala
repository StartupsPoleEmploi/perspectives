package controllers.conseiller

import play.api.data.Form
import play.api.data.Forms._

case class PaginationCandidatForm(dateInscription: Long,
                                  candidatId: String)

object PaginationCandidatForm {

  val form = Form(
    mapping(
      "dateInscription" -> longNumber,
      "candidatId" -> nonEmptyText
    )(PaginationCandidatForm.apply)(PaginationCandidatForm.unapply)
  )
}
