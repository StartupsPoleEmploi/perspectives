package controllers.candidat

import controllers.FormHelpers
import fr.poleemploi.perspectives.commun.domain.RayonRecherche
import fr.poleemploi.perspectives.projections.candidat.CandidatCriteresRechercheDto
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

case class SaisieCriteresRechercheForm(nouveauCandidat: Boolean,
                                       numeroTelephone: String,
                                       rechercheMetierEvalue: String,
                                       rechercheAutreMetier: String,
                                       metiersRecherches: Set[String],
                                       etreContacteParOrganismeFormation: String,
                                       etreContacteParAgenceInterim: String,
                                       rayonRecherche: Int)

object SaisieCriteresRechercheForm {

  val rayonRechercheConstraint: Constraint[Int] = Constraint("constraint.rayonRecherche")({
    n =>
      if (RayonRecherche.from(n).isDefined) {
        Valid
      } else {
        Invalid(Seq(ValidationError("constraint.rayonRecherche")))
      }
  })

  val form = Form(
    mapping(
      "nouveauCandidat" -> boolean,
      "numeroTelephone" -> nonEmptyText.verifying(FormHelpers.numeroTelephoneConstraint),
      "rechercheMetierEvalue" -> nonEmptyText,
      "rechercheAutreMetier" -> nonEmptyText,
      "listeMetiersRecherches" -> set(nonEmptyText),
      "contactFormation" -> nonEmptyText,
      "contactInterim" -> nonEmptyText,
      "rayonRecherche" -> number.verifying(rayonRechercheConstraint)
    )(SaisieCriteresRechercheForm.apply)(SaisieCriteresRechercheForm.unapply) verifying metiersSelectionnes
  )

  val nouveauCandidat: Form[SaisieCriteresRechercheForm] = SaisieCriteresRechercheForm.form.fill(
    SaisieCriteresRechercheForm(
      nouveauCandidat = true,
      rechercheMetierEvalue = "",
      rechercheAutreMetier = "",
      metiersRecherches = Set.empty,
      etreContacteParAgenceInterim = "",
      etreContacteParOrganismeFormation = "",
      rayonRecherche = 0,
      numeroTelephone = ""
    )
  )

  def fromCandidatCriteresRechercheDto(candidat: CandidatCriteresRechercheDto): Form[SaisieCriteresRechercheForm] = SaisieCriteresRechercheForm.form.fill(
    SaisieCriteresRechercheForm(
      nouveauCandidat = false,
      rechercheMetierEvalue = candidat.rechercheMetierEvalue.map(FormHelpers.booleanToString).getOrElse(""),
      rechercheAutreMetier = candidat.rechercheAutreMetier.map(FormHelpers.booleanToString).getOrElse(""),
      metiersRecherches = candidat.metiersRecherches.map(_.value).toSet,
      etreContacteParAgenceInterim = candidat.contacteParAgenceInterim.map(FormHelpers.booleanToString).getOrElse(""),
      etreContacteParOrganismeFormation = candidat.contacteParOrganismeFormation.map(FormHelpers.booleanToString).getOrElse(""),
      rayonRecherche = candidat.rayonRecherche.map(_.value).getOrElse(0),
      numeroTelephone = candidat.numeroTelephone.map(_.value).getOrElse("")
    )
  )

  def metiersSelectionnes: Constraint[SaisieCriteresRechercheForm] =
    Constraint {
      fields =>
        if (fields.rechercheAutreMetier == "true" && fields.metiersRecherches.isEmpty) {
          Invalid(ValidationError("constraint.criteres.selectionmetiers"))
        } else {
          Valid
        }
    }
}