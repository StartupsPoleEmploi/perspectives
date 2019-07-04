package fr.poleemploi.perspectives.emailing.domain

import scala.concurrent.Future

trait ImportProspectService {

  def importerProspectsCandidat: Future[Stream[MRSValideeProspectCandidat]]
}
