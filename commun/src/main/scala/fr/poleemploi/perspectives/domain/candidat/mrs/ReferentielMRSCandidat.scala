package fr.poleemploi.perspectives.domain.candidat.mrs

import fr.poleemploi.perspectives.domain.authentification.infra.peconnect.PEConnectId

import scala.concurrent.Future

trait ReferentielMRSCandidat {

  def integrerMRSValidees: Future[Unit]

  def metiersValidesParCandidat(peConnectId: PEConnectId): Future[List[MRSValidee]]
}
