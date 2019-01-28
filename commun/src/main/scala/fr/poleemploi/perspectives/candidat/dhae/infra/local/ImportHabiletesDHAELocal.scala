package fr.poleemploi.perspectives.candidat.dhae.infra.local

import fr.poleemploi.perspectives.candidat.dhae.domain.{HabiletesDHAE, ImportHabiletesDHAE}

import scala.concurrent.Future

class ImportHabiletesDHAELocal extends ImportHabiletesDHAE {
  
  override def integrerHabiletesDHAE: Future[Stream[HabiletesDHAE]] =
    Future.successful(Stream.empty)
}
