package authentification.infra.play

import fr.poleemploi.perspectives.domain.authentification.ConseillerAuthentifie
import play.api.mvc.Session

object SessionConseillerAuthentifie {

  // FIXME : se base sur l'authentification candidat le temps d'avoir une authentification propre
  def get(session: Session): Option[ConseillerAuthentifie] =
    for {
      conseillerId <- session.get("candidat.candidatId")
    } yield ConseillerAuthentifie(
      conseillerId = conseillerId
    )

  def set(conseillerAuthentifie: ConseillerAuthentifie,
          session: Session): Session =
    session + ("candidat.candidatId" -> conseillerAuthentifie.conseillerId)
}
