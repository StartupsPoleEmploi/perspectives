package fr.poleemploi.perspectives.candidat.mrs.infra.local

import fr.poleemploi.perspectives.candidat.mrs.domain.{ImportMRS, MRSValideeCandidat}

import scala.concurrent.Future

class ImportMRSLocalAdapter extends ImportMRS {

  override def integrerMRSValidees: Future[Stream[MRSValideeCandidat]] =
    Future.successful(Stream.empty)
}
