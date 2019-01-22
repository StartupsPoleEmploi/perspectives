package fr.poleemploi.perspectives.metier.domain

import fr.poleemploi.perspectives.commun.domain._

import scala.concurrent.Future

trait ReferentielMetier {

  def metiersParCode(codesROME: List[CodeROME]): Future[List[Metier]]
}

