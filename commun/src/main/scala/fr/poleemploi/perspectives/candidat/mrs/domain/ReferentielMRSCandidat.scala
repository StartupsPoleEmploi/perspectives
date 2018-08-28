package fr.poleemploi.perspectives.candidat.mrs.domain

import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId

import scala.concurrent.Future

trait ReferentielMRSCandidat {

  def integrerMRSValidees: Future[Unit]

  def metiersValidesParCandidat(peConnectId: PEConnectId): Future[List[MRSValidee]]
}
