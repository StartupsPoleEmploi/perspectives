package authentification

import fr.poleemploi.perspectives.authentification.domain.CandidatAuthentifie
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{Email, Nom, Prenom}
import play.api.mvc.Session

object SessionCandidatAuthentifie {

  private val namespace = "candidat"
  private val candidatIdAttribute: String = s"$namespace.candidatId"
  private val nomAttribute: String = s"$namespace.nom"
  private val prenomAttribute: String = s"$namespace.prenom"
  private val emailAttribute: String = s"$namespace.email"

  def get(session: Session): Option[CandidatAuthentifie] =
    for {
      candidatId <- session.get(candidatIdAttribute).map(CandidatId)
      nom <- session.get(nomAttribute).map(Nom(_))
      prenom <- session.get(prenomAttribute).map(Prenom(_))
    } yield CandidatAuthentifie(
      candidatId = candidatId,
      nom = nom,
      prenom = prenom,
      email = session.get(emailAttribute).map(Email(_))
    )

  def set(candidatAuthentifie: CandidatAuthentifie,
          session: Session): Session = {
    val newSession = session +
      (candidatIdAttribute -> candidatAuthentifie.candidatId.value) +
      (nomAttribute -> candidatAuthentifie.nom.value) +
      (prenomAttribute -> candidatAuthentifie.prenom.value)

    candidatAuthentifie.email.map(email =>
      newSession + (emailAttribute -> email.value)
    ).getOrElse(newSession)
  }

  def remove(session: Session): Session =
    session - candidatIdAttribute - nomAttribute - prenomAttribute - emailAttribute
}
