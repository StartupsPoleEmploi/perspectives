package fr.poleemploi.perspectives.candidat

import fr.poleemploi.eventsourcing.StringValueObject

case class CentreInteret private(value: String) extends StringValueObject

object CentreInteret {

  def apply(centreInteret: String): CentreInteret = new CentreInteret(centreInteret.capitalize)
}