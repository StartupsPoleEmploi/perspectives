package controllers.candidat

import controllers.FormHelpers
import fr.poleemploi.perspectives.commun.domain.RayonRecherche
import fr.poleemploi.perspectives.projections.candidat.CandidatSaisieCriteresRechercheDto
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

  val rayonRechercheConstraint: Constraint[Int] = Constraint("constraint.rayonRecherche")(
    n => RayonRecherche.from(n)
        .map(_ => Valid)
        .getOrElse(Invalid(Seq(ValidationError("constraint.rayonRecherche"))))
  )

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

  def fromCandidatCriteresRechercheDto(candidat: CandidatSaisieCriteresRechercheDto): Form[SaisieCriteresRechercheForm] = SaisieCriteresRechercheForm.form.fill(
    SaisieCriteresRechercheForm(
      nouveauCandidat = false,
      rechercheMetierEvalue = FormHelpers.optBooleanToString(candidat.rechercheMetierEvalue),
      rechercheAutreMetier = FormHelpers.optBooleanToString(candidat.rechercheAutreMetier),
      metiersRecherches = candidat.metiersRecherches.map(_.value).toSet,
      etreContacteParAgenceInterim = FormHelpers.optBooleanToString(candidat.contacteParAgenceInterim),
      etreContacteParOrganismeFormation = FormHelpers.optBooleanToString(candidat.contacteParOrganismeFormation),
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