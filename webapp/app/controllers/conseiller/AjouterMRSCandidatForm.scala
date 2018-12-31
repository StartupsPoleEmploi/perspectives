package controllers.conseiller

import java.time.LocalDate

import play.api.data.Form
import play.api.data.Forms.{localDate, mapping, nonEmptyText}

case class AjouterMRSCandidatForm(candidatId: String,
                                  codeROME: String,
                                  dateEvaluation: LocalDate,
                                  codeDepartement: String)

object AjouterMRSCandidatForm {

  val form = Form(
    mapping(
      "candidatId" -> nonEmptyText,
      "codeROME" -> nonEmptyText,
      "dateEvaluation" -> localDate("yyyy-MM-dd"),
      "codeDepartement" -> nonEmptyText
    )(AjouterMRSCandidatForm.apply)(AjouterMRSCandidatForm.unapply)
  )

}