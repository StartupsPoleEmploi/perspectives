package fr.poleemploi.perspectives.candidat.mrs.infra.local

import fr.poleemploi.perspectives.candidat.mrs.domain.ImportMRSDHAE
import fr.poleemploi.perspectives.emailing.domain.MRSDHAEValideeProspectCandidat

import scala.concurrent.Future

class ImportMRSDHAELocalAdapter extends ImportMRSDHAE {

  override def importerProspectsCandidats: Future[Stream[MRSDHAEValideeProspectCandidat]] =
    Future.successful(Stream.empty)
}
