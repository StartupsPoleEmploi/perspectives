package fr.poleemploi.perspectives.metier.domain

import fr.poleemploi.perspectives.commun.domain._

// TODO : à implémenter
trait ReferentielMetier {

  def metierParCode(code: CodeROME): Metier
}
