package fr.poleemploi.perspectives.candidat.mrs.infra.local

import fr.poleemploi.perspectives.candidat.mrs.domain.{HabiletesMRS, ImportHabiletesMRS}

import scala.concurrent.Future

class ImportHabiletesMRSLocal extends ImportHabiletesMRS {

  override def integrerHabiletesMRS: Future[Stream[HabiletesMRS]] =
    Future.successful(Stream.empty)
}
