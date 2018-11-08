package fr.poleemploi.perspectives.metier.domain

import fr.poleemploi.perspectives.commun.domain._

trait ReferentielMetier {

  def metierParCode(code: CodeROME): Metier
}

