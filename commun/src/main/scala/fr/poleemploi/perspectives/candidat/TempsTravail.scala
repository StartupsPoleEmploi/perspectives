package fr.poleemploi.perspectives.candidat

import fr.poleemploi.eventsourcing.StringValueObject

case class TempsTravail private(value: String) extends StringValueObject

object TempsTravail {

  val TEMPS_PLEIN = new TempsTravail("TEMPS_PLEIN")
  val TEMPS_PARTIEL = new TempsTravail("TEMPS_PARTIEL")
}
