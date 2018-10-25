package fr.poleemploi.perspectives.candidat.mrs.infra.local

import fr.poleemploi.perspectives.candidat.mrs.domain.{ImportMRSCandidat, MRSValideeCandidat}

import scala.concurrent.Future

class ImportMRSCandidatLocal extends ImportMRSCandidat {

  override def integrerMRSValidees: Future[Stream[MRSValideeCandidat]] =
    Future.successful(Stream.empty)
}
