package controllers.candidat

import java.time.{LocalDate, Period}

import controllers.FormHelpers
import fr.poleemploi.perspectives.projections.candidat.CandidatSaisieDisponibilitesQueryResult
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.libs.json.{Json, Writes}

case class SaisieDisponibilitesForm(candidatEnRecherche: String,
                                    disponibiliteConnue: Option[String],
                                    nbMoisProchaineDisponibilite: Option[Int],
                                    emploiTrouveGracePerspectives: Option[String])

object SaisieDisponibilitesForm {

  implicit val writes: Writes[SaisieDisponibilitesForm] = Json.writes[SaisieDisponibilitesForm]

  def disponibiliteConstraint: Constraint[SaisieDisponibilitesForm] =
    Constraint(
      fields =>
        if (isDisponibilitePasRenseignee(fields.candidatEnRecherche, fields.disponibiliteConnue, fields.nbMoisProchaineDisponibilite)) {
          Invalid(ValidationError("constraint.criteres.disponibiliteConnue"))
        } else if (fields.candidatEnRecherche == "false" && fields.emploiTrouveGracePerspectives.isEmpty) {
          Invalid(ValidationError("constraint.criteres.emploiTrouveGracePerspectives"))
        } else {
          Valid
        }
    )

  val form = Form(
    mapping(
      "candidatEnRecherche" -> nonEmptyText,
      "disponibiliteConnue" -> optional(nonEmptyText),
      "nbMoisProchaineDisponibilite" -> optional(number),
      "emploiTrouveGracePerspectives" -> optional(nonEmptyText)
    )(SaisieDisponibilitesForm.apply)(SaisieDisponibilitesForm.unapply) verifying disponibiliteConstraint
  )

  def nouvellesDisponibilites: Form[SaisieDisponibilitesForm] = SaisieDisponibilitesForm.form.fill(
    SaisieDisponibilitesForm(
      candidatEnRecherche = "",
      disponibiliteConnue = None,
      nbMoisProchaineDisponibilite = None,
      emploiTrouveGracePerspectives = None
    )
  )

  def fromCandidatDisponibilitesQueryResult(candidat: CandidatSaisieDisponibilitesQueryResult): Form[SaisieDisponibilitesForm] = SaisieDisponibilitesForm.form.fill(
    SaisieDisponibilitesForm(
      candidatEnRecherche = FormHelpers.optBooleanToString(Some(candidat.candidatEnRecherche)),
      disponibiliteConnue = Some(FormHelpers.optBooleanToString(Some(candidat.dateProchaineDisponibilite.isDefined))),
      nbMoisProchaineDisponibilite = candidat.dateProchaineDisponibilite.map(nbMoisAvantProchaineDisponibilite),
      emploiTrouveGracePerspectives = Some(FormHelpers.optBooleanToString(Some(candidat.emploiTrouveGracePerspectives)))
    )
  )

  private def nbMoisAvantProchaineDisponibilite(dateProchaineDisponibilite: LocalDate): Int = {
    val nbMois = Period.between(LocalDate.now(), dateProchaineDisponibilite).getMonths
    if (nbMois <= 0) 0
    else nbMois
  }

  private[candidat] def isDisponibilitePasRenseignee(candidatEnRecherche: String,
                                                     disponibiliteConnue: Option[String],
                                                     nbMoisProchaineDisponibilite: Option[Int]): Boolean =
    "false".equals(candidatEnRecherche) &&
      (disponibiliteConnue.isEmpty ||
        ("true".equals(disponibiliteConnue.get) && nbMoisProchaineDisponibilite.isEmpty))

  private[candidat] def isEmploiTrouvePasRenseigne(candidatEnRecherche: String,
                                                   emploiTrouveGracePerspectives: Option[String]): Boolean =
    "false".equals(candidatEnRecherche) && emploiTrouveGracePerspectives.isEmpty
}
