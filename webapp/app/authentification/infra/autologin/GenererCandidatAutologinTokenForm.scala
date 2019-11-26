package authentification.infra.autologin

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}

case class GenererCandidatAutologinTokenForm(candidatId: String,
                                             nom: String,
                                             prenom: String,
                                             email: String)

object GenererCandidatAutologinTokenForm {

  val form = Form(
    mapping(
      "candidatId" -> nonEmptyText,
      "nom" -> nonEmptyText,
      "prenom" -> nonEmptyText,
      "email" -> nonEmptyText
    )(GenererCandidatAutologinTokenForm.apply)(GenererCandidatAutologinTokenForm.unapply)
  )

}
