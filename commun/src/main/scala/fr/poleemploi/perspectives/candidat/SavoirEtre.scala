package fr.poleemploi.perspectives.candidat

import fr.poleemploi.eventsourcing.StringValueObject

case class SavoirEtre private(value: String) extends StringValueObject

object SavoirEtre {

  def apply(savoirEtre: String): SavoirEtre = new SavoirEtre(savoirEtre.toLowerCase.capitalize)
}