package controllers.conseiller

import java.time.{LocalDate, ZonedDateTime}

import controllers.FormHelpers._
import play.api.data.Form
import play.api.data.Forms.{mapping, of, _}

case class PaginationRecruteursForm(dateInscription: ZonedDateTime,
                                    recruteurId: String)

case class RechercheRecruteursForm(codesDepartement: List[String],
                                   codePostal: Option[String],
                                   dateDebut: Option[LocalDate],
                                   dateFin: Option[LocalDate],
                                   typeRecruteur: Option[String],
                                   contactParCandidats: Option[Boolean],
                                   pagination: Option[PaginationRecruteursForm])

object RechercheRecruteursForm {

  val form = Form(
    mapping(
      "codesDepartement" -> list(text),
      "codePostal" -> optional(text),
      "dateDebut" -> optional(localDate),
      "dateFin" -> optional(localDate),
      "typeRecruteur" -> optional(text),
      "contactParCandidats" -> optional(boolean),
      "page" -> optional(mapping(
        "dateInscription" -> of[ZonedDateTime],
        "recruteurId" -> nonEmptyText
      )(PaginationRecruteursForm.apply)(PaginationRecruteursForm.unapply))
    )(RechercheRecruteursForm.apply)(RechercheRecruteursForm.unapply)
  )
}