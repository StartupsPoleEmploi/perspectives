package authentification

import fr.poleemploi.perspectives.authentification.domain.CandidatAuthentifie
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{Nom, Prenom}
import play.api.mvc.Session

object SessionCandidatAuthentifie {

  private val namespace = "candidat"
  private val candidatIdAttribute: String = s"$namespace.candidatId"
  private val nomAttribute: String = s"$namespace.nom"
  private val prenomAttribute: String = s"$namespace.prenom"

  def get(session: Session): Option[CandidatAuthentifie] =
    for {
      candidatId <- session.get(candidatIdAttribute).map(CandidatId)
      nom <- session.get(nomAttribute).map(Nom(_))
      prenom <- session.get(prenomAttribute).map(Prenom(_))
    } yield CandidatAuthentifie(
      candidatId = candidatId,
      nom = nom,
      prenom = prenom
    )

  def set(candidatAuthentifie: CandidatAuthentifie,
          session: Session): Session =
    session + (candidatIdAttribute -> candidatAuthentifie.candidatId.value) + (nomAttribute -> candidatAuthentifie.nom.value) + (prenomAttribute -> candidatAuthentifie.prenom.value)

  def remove(session: Session): Session =
    session - candidatIdAttribute - nomAttribute - prenomAttribute
}
