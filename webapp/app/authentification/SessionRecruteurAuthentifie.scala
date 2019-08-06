package authentification

import fr.poleemploi.perspectives.authentification.domain.RecruteurAuthentifie
import fr.poleemploi.perspectives.commun.domain.{Nom, Prenom}
import fr.poleemploi.perspectives.recruteur.RecruteurId
import play.api.mvc.Session

object SessionRecruteurAuthentifie {

  private val namespace = "recruteur"
  private val recruteurIdAttribute: String = s"$namespace.recruteurId"
  private val nomAttribute: String = s"$namespace.nom"
  private val prenomAttribute: String = s"$namespace.prenom"

  def get(session: Session): Option[RecruteurAuthentifie] =
    for {
      recruteurId <- session.get(recruteurIdAttribute).map(RecruteurId)
      nom <- session.get(nomAttribute).map(Nom(_))
      prenom <- session.get(prenomAttribute).map(Prenom(_))
    } yield RecruteurAuthentifie(
      recruteurId = recruteurId,
      nom = nom,
      prenom = prenom
    )

  def set(recruteurAuthentifie: RecruteurAuthentifie,
          session: Session): Session =
    session + (recruteurIdAttribute -> recruteurAuthentifie.recruteurId.value) + (nomAttribute -> recruteurAuthentifie.nom.value) + (prenomAttribute -> recruteurAuthentifie.prenom.value)

  def remove(session: Session): Session =
    session - recruteurIdAttribute - nomAttribute - prenomAttribute
}