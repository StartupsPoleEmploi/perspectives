package fr.poleemploi.perspectives.candidat.mrs.domain

import scala.concurrent.Future

trait ImportMRS {

  def integrerMRSValidees: Future[Unit]

  def integrerMRSDHAEValidees: Future[Unit]
}
