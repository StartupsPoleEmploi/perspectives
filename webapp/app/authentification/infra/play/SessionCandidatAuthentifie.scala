package authentification.infra.play

import authentification.model.CandidatAuthentifie
import play.api.mvc.Session

object SessionCandidatAuthentifie {

  private val namespace = "candidat"
  private val candidatIdAttribute: String = s"$namespace.candidatId"
  private val nomAttribute: String = s"$namespace.nom"
  private val prenomAttribute: String = s"$namespace.prenom"

  def get(session: Session): Option[CandidatAuthentifie] =
    for {
      candidatId <- session.get(candidatIdAttribute)
      nom <- session.get(nomAttribute)
      prenom <- session.get(prenomAttribute)
    } yield CandidatAuthentifie(
      candidatId = candidatId,
      nom = nom,
      prenom = prenom
    )

  def set(candidatAuthentifie: CandidatAuthentifie,
          session: Session): Session =
    session + (candidatIdAttribute -> candidatAuthentifie.candidatId) + (nomAttribute -> candidatAuthentifie.nom) + (prenomAttribute -> candidatAuthentifie.prenom)

  def remove(session: Session): Session =
    session - candidatIdAttribute - nomAttribute - prenomAttribute
}
