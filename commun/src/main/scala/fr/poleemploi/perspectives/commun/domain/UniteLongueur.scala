package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

case class UniteLongueur(value: String) extends StringValueObject

object UniteLongueur {

  val KM = UniteLongueur("KM")

  private val values: Map[String, UniteLongueur] = Map(
    KM.value -> KM
  )

  def from(value: String): Option[UniteLongueur] = values.get(value)
}
