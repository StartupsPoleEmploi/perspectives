package fr.poleemploi.perspectives.candidat.mrs.domain

import fr.poleemploi.perspectives.candidat.CandidatId

import scala.concurrent.Future

trait ReferentielMRSCandidat {

  def integrerMRSValidees: Future[Stream[MRSValideeCandidat]]

  def mrsValideesParCandidat(candidatId: CandidatId): Future[List[MRSValidee]]
}
