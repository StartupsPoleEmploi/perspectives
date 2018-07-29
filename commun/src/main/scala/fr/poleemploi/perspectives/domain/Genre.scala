package fr.poleemploi.perspectives.domain

import fr.poleemploi.eventsourcing.StringValueObject

import scala.collection.immutable.ListMap

/**
  * Value Object Genre
  */
case class Genre(value: String, label: String) extends StringValueObject

/**
  * Methodes pour construire et valider un Genre
  */
object Genre {

  val HOMME = Genre(value = "H", "Homme")
  val FEMME = Genre(value = "F", "Femme")

  private val values = ListMap(
    HOMME.value -> HOMME,
    FEMME.value -> FEMME,
  )

  def from(r: String): Option[Genre] = values.get(r)
}