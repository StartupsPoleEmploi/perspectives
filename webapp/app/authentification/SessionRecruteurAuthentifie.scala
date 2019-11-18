package authentification

import fr.poleemploi.perspectives.authentification.domain.RecruteurAuthentifie
import fr.poleemploi.perspectives.commun.domain.{Email, Nom, Prenom}
import fr.poleemploi.perspectives.recruteur.{RecruteurId, TypeRecruteur}
import play.api.mvc.Session

object SessionRecruteurAuthentifie {

  private val namespace = "recruteur"
  private val recruteurIdAttribute: String = s"$namespace.recruteurId"
  private val nomAttribute: String = s"$namespace.nom"
  private val prenomAttribute: String = s"$namespace.prenom"
  private val certifieAttribute: String = s"$namespace.certifie"
  private val typeRecruteurAttribute: String = s"$namespace.typeRecruteur"
  private val emailAttribute: String = s"$namespace.email"

  def get(session: Session): Option[RecruteurAuthentifie] =
    for {
      recruteurId <- session.get(recruteurIdAttribute).map(RecruteurId)
      nom <- session.get(nomAttribute).map(Nom(_))
      prenom <- session.get(prenomAttribute).map(Prenom(_))
      email <- session.get(emailAttribute).map(Email(_))
    } yield RecruteurAuthentifie(
      recruteurId = recruteurId,
      nom = nom,
      prenom = prenom,
      email = email,
      certifie = session.get(certifieAttribute).exists(_.toBoolean),
      typeRecruteur = session.get(typeRecruteurAttribute).map(TypeRecruteur(_)),
    )

  def set(recruteurAuthentifie: RecruteurAuthentifie,
          session: Session): Session = {
    val newSession = session +
      (recruteurIdAttribute -> recruteurAuthentifie.recruteurId.value) +
      (nomAttribute -> recruteurAuthentifie.nom.value) +
      (prenomAttribute -> recruteurAuthentifie.prenom.value) +
      (emailAttribute -> recruteurAuthentifie.email.value) +
      (certifieAttribute -> recruteurAuthentifie.certifie.toString)

    recruteurAuthentifie.typeRecruteur.map(typeRecruteur =>
      newSession + (typeRecruteurAttribute -> typeRecruteur.value)
    ).getOrElse(newSession)
  }

  def remove(session: Session): Session =
    session - recruteurIdAttribute - nomAttribute - prenomAttribute - emailAttribute - certifieAttribute - typeRecruteurAttribute
}
