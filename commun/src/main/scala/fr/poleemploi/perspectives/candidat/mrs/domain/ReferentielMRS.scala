package fr.poleemploi.perspectives.candidat.mrs.domain

import fr.poleemploi.perspectives.candidat.CandidatId

import scala.concurrent.Future

trait ReferentielMRS {

  def mrsValidees(candidatId: CandidatId): Future[List[MRSValidee]]
}
