package controllers.conseiller

import controllers.FormHelpers._
import java.time.ZonedDateTime

import play.api.data.Form
import play.api.data.Forms._

case class PaginationRecruteurForm(dateInscription: ZonedDateTime,
                                   recruteurId: String)

object PaginationRecruteurForm {

  val form = Form(
    mapping(
      "dateInscription" -> of[ZonedDateTime],
      "recruteurId" -> nonEmptyText
    )(PaginationRecruteurForm.apply)(PaginationRecruteurForm.unapply)
  )
}