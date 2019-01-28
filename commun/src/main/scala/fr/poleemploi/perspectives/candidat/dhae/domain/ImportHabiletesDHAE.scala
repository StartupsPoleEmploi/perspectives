package fr.poleemploi.perspectives.candidat.dhae.domain

import scala.concurrent.Future

trait ImportHabiletesDHAE {

  def integrerHabiletesDHAE: Future[Stream[HabiletesDHAE]]

}
