package controllers.candidat

import controllers.FormHelpers
import fr.poleemploi.perspectives.commun.domain.RayonRecherche
import fr.poleemploi.perspectives.projections.candidat.CandidatDto
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

  val mediaTypesValides = List(
    "application/pdf",
    "application/vnd.oasis.opendocument.text",
    "image/jpeg",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
  )

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

  def fromCandidat(candidatDto: CandidatDto): Form[SaisieCriteresRechercheForm] = SaisieCriteresRechercheForm.form.fill(
    SaisieCriteresRechercheForm(
      nouveauCandidat = false,
      rechercheMetierEvalue = candidatDto.rechercheMetierEvalue.map(FormHelpers.booleanToString).getOrElse(""),
      rechercheAutreMetier = candidatDto.rechercheAutreMetier.map(FormHelpers.booleanToString).getOrElse(""),
      metiersRecherches = candidatDto.metiersRecherches.map(_.value).toSet,
      etreContacteParAgenceInterim = candidatDto.contacteParAgenceInterim.map(FormHelpers.booleanToString).getOrElse(""),
      etreContacteParOrganismeFormation = candidatDto.contacteParOrganismeFormation.map(FormHelpers.booleanToString).getOrElse(""),
      rayonRecherche = candidatDto.rayonRecherche.map(_.value).getOrElse(0),
      numeroTelephone = candidatDto.numeroTelephone.map(_.value).getOrElse("")
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