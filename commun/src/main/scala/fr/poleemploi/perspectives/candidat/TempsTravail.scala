package fr.poleemploi.perspectives.candidat

import fr.poleemploi.eventsourcing.StringValueObject

case class TempsTravail private(value: String) extends StringValueObject

object TempsTravail {

  val TEMPS_PLEIN = TempsTravail("TEMPS_PLEIN")
  val TEMPS_PARTIEL = TempsTravail("TEMPS_PARTIEL")

  private val values: Map[String, TempsTravail] = Map(
    TEMPS_PLEIN.value -> TEMPS_PLEIN,
    TEMPS_PARTIEL.value -> TEMPS_PARTIEL
  )

  def from(value: String): Option[TempsTravail] = values.get(value)
}
