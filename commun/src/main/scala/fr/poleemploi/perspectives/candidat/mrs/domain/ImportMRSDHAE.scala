package fr.poleemploi.perspectives.candidat.mrs.domain

import fr.poleemploi.perspectives.emailing.domain.MRSDHAEValideeProspectCandidat

import scala.concurrent.Future

trait ImportMRSDHAE {

  def importerProspectsCandidats: Future[Stream[MRSDHAEValideeProspectCandidat]]
}
