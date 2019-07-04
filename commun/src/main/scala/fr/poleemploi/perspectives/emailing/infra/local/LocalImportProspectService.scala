package fr.poleemploi.perspectives.emailing.infra.local

import fr.poleemploi.perspectives.emailing.domain.{ImportProspectService, MRSValideeProspectCandidat}

import scala.concurrent.Future

class LocalImportProspectService extends ImportProspectService {

  override def importerProspectsCandidat: Future[Stream[MRSValideeProspectCandidat]] =
    Future.successful(Stream.empty)
}
