package authentification.infra.play

import authentification.model.RecruteurAuthentifie
import play.api.mvc.Session

object SessionRecruteurAuthentifie {

  private val namespace = "recruteur"
  private val recruteurIdAttribute: String = s"$namespace.recruteurId"
  private val nomAttribute: String = s"$namespace.nom"
  private val prenomAttribute: String = s"$namespace.prenom"

  def get(session: Session): Option[RecruteurAuthentifie] =
    for {
      recruteurId <- session.get(recruteurIdAttribute)
      nom <- session.get(nomAttribute)
      prenom <- session.get(prenomAttribute)
    } yield RecruteurAuthentifie(
      recruteurId = recruteurId,
      nom = nom,
      prenom = prenom
    )

  def set(recruteurAuthentifie: RecruteurAuthentifie,
          session: Session): Session =
    session + (recruteurIdAttribute -> recruteurAuthentifie.recruteurId) + (nomAttribute -> recruteurAuthentifie.nom) + (prenomAttribute -> recruteurAuthentifie.prenom)

  def remove(session: Session): Session =
    session - recruteurIdAttribute - nomAttribute - prenomAttribute
}
