package controllers.conseiller

import java.time.LocalDate

import play.api.data.Form
import play.api.data.Forms.{localDate, mapping, nonEmptyText, boolean}

case class AjouterMRSCandidatForm(candidatId: String,
                                  codeROME: String,
                                  dateEvaluation: LocalDate,
                                  codeDepartement: String,
                                  isDHAE: Boolean)

object AjouterMRSCandidatForm {

  val form = Form(
    mapping(
      "candidatId" -> nonEmptyText,
      "codeROME" -> nonEmptyText,
      "dateEvaluation" -> localDate("yyyy-MM-dd"),
      "codeDepartement" -> nonEmptyText,
      "isDHAE" -> boolean
    )(AjouterMRSCandidatForm.apply)(AjouterMRSCandidatForm.unapply)
  )

}
