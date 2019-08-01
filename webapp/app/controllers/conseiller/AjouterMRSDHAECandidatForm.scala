package controllers.conseiller

import java.time.LocalDate

import play.api.data.Form
import play.api.data.Forms.{localDate, mapping, nonEmptyText}

case class AjouterMRSDHAECandidatForm(candidatId: String,
                                      codeROME: String,
                                      dateEvaluation: LocalDate,
                                      codeDepartement: String)

object AjouterMRSDHAECandidatForm {

  val form = Form(
    mapping(
      "candidatId" -> nonEmptyText,
      "codeROME" -> nonEmptyText,
      "dateEvaluation" -> localDate("yyyy-MM-dd"),
      "codeDepartement" -> nonEmptyText
    )(AjouterMRSDHAECandidatForm.apply)(AjouterMRSDHAECandidatForm.unapply)
  )

}
