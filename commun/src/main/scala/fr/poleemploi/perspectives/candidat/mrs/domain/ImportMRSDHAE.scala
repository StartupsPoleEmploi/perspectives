package fr.poleemploi.perspectives.candidat.mrs.domain

import scala.concurrent.Future

trait ImportMRSDHAE {

  def integrerMRSDHAEValidees: Future[Unit]
}
