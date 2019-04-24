package authentification.infra.play

import fr.poleemploi.perspectives.authentification.domain.ConseillerAuthentifie
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.conseiller.ConseillerId
import play.api.mvc.Session

// Se base sur l'authentification candidat le temps d'avoir une authentification propre
class SessionConseillerAuthentifie(candidatsConseillers: Map[CandidatId, ConseillerId]) {

  def get(session: Session): Option[ConseillerAuthentifie] =
    session.get("candidat.candidatId").flatMap(id => candidatsConseillers.get(CandidatId(id))).map(ConseillerAuthentifie)

  def set(conseillerAuthentifie: ConseillerAuthentifie,
          session: Session): Session =
    session + ("candidat.candidatId" -> conseillerAuthentifie.conseillerId.value)
}
