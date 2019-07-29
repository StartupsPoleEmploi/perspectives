package controllers.recruteur

import controllers.FormHelpers
import fr.poleemploi.perspectives.projections.recruteur.ProfilRecruteurQueryResult
import fr.poleemploi.perspectives.recruteur.{NumeroSiret, TypeRecruteur}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.libs.json.{Json, Writes}

case class AdresseForm(commune: String,
                       codePostal: String,
                       pays: String)

object AdresseForm {

  implicit val writes: Writes[AdresseForm] = Json.writes[AdresseForm]
}

case class ProfilForm(nouveauRecruteur: Boolean,
                      typeRecruteur: String,
                      raisonSociale: String,
                      adresse: AdresseForm,
                      numeroSiret: String,
                      numeroTelephone: String,
                      contactParCandidats: String)

object ProfilForm {

  implicit val writes: Writes[ProfilForm] = Json.writes[ProfilForm]

  val numeroSiretConstraint: Constraint[String] = Constraint("constraint.numeroSiret")(
    text => NumeroSiret.from(text)
      .map(_ => Valid)
      .getOrElse(Invalid(Seq(ValidationError("constraint.numeroSiret"))))
  )

  val typeRecruteurConstraint: Constraint[String] = Constraint("constraint.typeRecruteur")(
    text => TypeRecruteur.from(text)
      .map(_ => Valid)
      .getOrElse(Invalid(Seq(ValidationError("constraint.typeRecruteur"))))
  )

  val form = Form(
    mapping(
      "nouveauRecruteur" -> boolean,
      "typeRecruteur" -> nonEmptyText.verifying(typeRecruteurConstraint),
      "raisonSociale" -> nonEmptyText,
      "adresse" -> mapping(
        "commune" -> nonEmptyText,
        "codePostal" -> nonEmptyText,
        "pays" -> nonEmptyText
      )(AdresseForm.apply)(AdresseForm.unapply),
      "numeroSiret" -> nonEmptyText.verifying(numeroSiretConstraint),
      "numeroTelephone" -> nonEmptyText.verifying(FormHelpers.numeroTelephoneConstraint),
      "contactParCandidats" -> nonEmptyText
    )(ProfilForm.apply)(ProfilForm.unapply)
  )

  val nouveauRecruteur: Form[ProfilForm] = ProfilForm.form.fill(
    ProfilForm(
      nouveauRecruteur = true,
      typeRecruteur = "",
      raisonSociale = "",
      adresse = AdresseForm(
        codePostal = "",
        commune = "",
        pays = ""
      ),
      numeroSiret = "",
      numeroTelephone = "",
      contactParCandidats = ""
    )
  )

  def fromProfilRecruteurQueryResult(profilRecruteur: ProfilRecruteurQueryResult): Form[ProfilForm] = ProfilForm.form.fill(
    ProfilForm(
      nouveauRecruteur = false,
      typeRecruteur = profilRecruteur.typeRecruteur.map(_.value).getOrElse(""),
      raisonSociale = profilRecruteur.raisonSociale.getOrElse(""),
      adresse = AdresseForm(
        codePostal = profilRecruteur.adresse.map(_.codePostal).getOrElse(""),
        commune = profilRecruteur.adresse.map(_.libelleCommune).getOrElse(""),
        pays = profilRecruteur.adresse.map(_.libellePays).getOrElse("")
      ),
      numeroSiret = profilRecruteur.numeroSiret.map(_.value).getOrElse(""),
      numeroTelephone = profilRecruteur.numeroTelephone.map(_.value).getOrElse(""),
      contactParCandidats = FormHelpers.optBooleanToString(profilRecruteur.contactParCandidats)
    )
  )
}