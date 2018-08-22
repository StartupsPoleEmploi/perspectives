package fr.poleemploi.perspectives.domain.metier

import scala.concurrent.Future

// TODO : à implémenter
trait ReferentielMetier {

  def getMetiers: Future[Unit]
}
