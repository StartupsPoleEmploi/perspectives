package fr.poleemploi.perspectives.candidat.mrs.domain

import scala.concurrent.Future

trait ImportMRSCandidat {

  def integrerMRSValidees: Future[Stream[MRSValideeCandidat]]
}
