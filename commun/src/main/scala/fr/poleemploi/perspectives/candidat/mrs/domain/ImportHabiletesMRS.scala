package fr.poleemploi.perspectives.candidat.mrs.domain

import scala.concurrent.Future

trait ImportHabiletesMRS {

  def integrerHabiletesMRS: Future[Stream[HabiletesMRS]]
}
