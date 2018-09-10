package fr.poleemploi.perspectives.candidat.mrs.domain

import fr.poleemploi.perspectives.candidat.CandidatId

import scala.concurrent.Future

trait ReferentielMRSCandidat {

  def integrerMRSValidees: Future[Unit]

  def mrsValideesParCandidat(candidatId: CandidatId): Future[List[MRSValidee]]
}
