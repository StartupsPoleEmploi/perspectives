package fr.poleemploi.perspectives.emailing.domain

import scala.concurrent.Future

trait ImportProspectService {

  def importerProspectsCandidats: Future[Stream[MRSValideeProspectCandidat]]
}
