package fr.poleemploi.perspectives.metier.domain

import scala.concurrent.Future

// TODO : à implémenter
trait ReferentielMetier {

  def getMetiers: Future[Unit]
}
