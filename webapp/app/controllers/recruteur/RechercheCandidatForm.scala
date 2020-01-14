package controllers.recruteur

import fr.poleemploi.perspectives.commun.domain.Coordonnees
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._

case class PaginationCandidatForm(score: Option[Int],
                                  dateDerniereMajDisponibilite: Long,
                                  candidatId: String)

case class RechercheCandidatForm(secteurActivite: Option[String],
                                 metier: Option[String],
                                 coordonnees: Option[Coordonnees],
                                 pagination: Option[PaginationCandidatForm])

object RechercheCandidatForm {

  val form = Form(
    mapping(
      "secteurActivite" -> optional(text),
      "metier" -> optional(text),
      "coordonnees" -> optional(mapping(
        "latitude" -> of[Double],
        "longitude" -> of[Double]
      )(Coordonnees.apply)(Coordonnees.unapply)),
      "pagination" -> optional(mapping(
        "score" -> optional(number),
        "dateDerniereMajDisponibilite" -> of[Long],
        "candidatId" -> text
      )(PaginationCandidatForm.apply)(PaginationCandidatForm.unapply))
    )(RechercheCandidatForm.apply)(RechercheCandidatForm.unapply)
  )
}
