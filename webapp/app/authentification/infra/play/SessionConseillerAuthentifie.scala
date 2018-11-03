package authentification.infra.play

import fr.poleemploi.perspectives.authentification.domain.ConseillerAuthentifie
import fr.poleemploi.perspectives.conseiller.ConseillerId
import play.api.mvc.Session

object SessionConseillerAuthentifie {

  // FIXME : se base sur l'authentification candidat le temps d'avoir une authentification propre
  val mapCandidatsConseillers = Map(
    "8cfee345-dabc-4830-ac6e-ad70418bfcbf" -> ConseillerId("b3c401e5-78a6-4889-8436-0d40e6b4c7b3"),
    "3a1ff0ac-43af-407e-a49a-719802e6eacf" -> ConseillerId("b3c401e5-78a6-4889-8436-0d40e6b4c7b3"),
    "17a57371-221b-4f97-b290-3fc80c6decdc" -> ConseillerId("b3c401e5-78a6-4889-8436-0d40e6b4c7b3")
  )

  def get(session: Session): Option[ConseillerAuthentifie] =
    session.get("candidat.candidatId").flatMap(mapCandidatsConseillers.get).map(ConseillerAuthentifie)

  def set(conseillerAuthentifie: ConseillerAuthentifie,
          session: Session): Session =
    session + ("candidat.candidatId" -> conseillerAuthentifie.conseillerId.value)
}
