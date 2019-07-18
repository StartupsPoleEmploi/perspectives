package controllers.candidat

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{Json, Writes}

case class PageOffresForm(debut: Int,
                          fin: Int)

case class LocalisationOffresForm(codePostal: String,
                                  lieuTravail: String,
                                  rayonRecherche: Option[Int])

case class RechercheOffresForm(motCle: Option[String],
                               localisation: Option[LocalisationOffresForm],
                               typesContrats: List[String],
                               metiers: List[String],
                               page: Option[PageOffresForm])

object RechercheOffresForm {

  implicit val writesPagesOffresForm: Writes[PageOffresForm] = Json.writes[PageOffresForm]
  implicit val writesLocalisationOffresForm: Writes[LocalisationOffresForm] = Json.writes[LocalisationOffresForm]
  implicit val writesRechercheOffresForm: Writes[RechercheOffresForm] = Json.writes[RechercheOffresForm]

  val form = Form(
    mapping(
      "motCle" -> optional(text),
      "localisation" -> optional(mapping(
        "codePostal" -> nonEmptyText,
        "lieuTravail" -> nonEmptyText,
        "rayonRecherche" -> optional(number)
      )(LocalisationOffresForm.apply)(LocalisationOffresForm.unapply)),
      "typesContrats" -> list(text),
      "metiers" -> list(text),
      "page" -> optional(mapping(
        "debut" -> number,
        "fin" -> number
      )(PageOffresForm.apply)(PageOffresForm.unapply))
    )(RechercheOffresForm.apply)(RechercheOffresForm.unapply)
  )
}
