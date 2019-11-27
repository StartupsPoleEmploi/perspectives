package controllers.conseiller

import java.time.LocalDate

import play.api.data.Form
import play.api.data.Forms.{mapping, _}

case class PaginationCandidatsForm(dateInscription: Long,
                                   candidatId: String)

case class RechercheCandidatsForm(codesDepartement: List[String],
                                  codePostal: Option[String],
                                  rechercheParLieuHabitation: Option[Boolean],
                                  rechercheParSecteurMrs: Option[Boolean],
                                  rechercheParDateInscription: Option[Boolean],
                                  dateDebut: Option[LocalDate],
                                  dateFin: Option[LocalDate],
                                  codeSecteurActivite: Option[String],
                                  pagination: Option[PaginationCandidatsForm])

object RechercheCandidatsForm {

  val form = Form(
    mapping(
      "codesDepartement" -> list(text),
      "codePostal" -> optional(text),
      "rechercheParLieuHabitation" -> optional(boolean),
      "rechercheParSecteurMrs" -> optional(boolean),
      "rechercheParDateInscription" -> optional(boolean),
      "dateDebut" -> optional(localDate),
      "dateFin" -> optional(localDate),
      "codeSecteurActivite" -> optional(text),
      "page" -> optional(mapping(
        "dateInscription" -> longNumber,
        "candidatId" -> nonEmptyText
      )(PaginationCandidatsForm.apply)(PaginationCandidatsForm.unapply))
    )(RechercheCandidatsForm.apply)(RechercheCandidatsForm.unapply)
  )
}
