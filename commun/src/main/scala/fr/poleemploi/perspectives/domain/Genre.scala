package fr.poleemploi.perspectives.domain

import scala.collection.immutable.ListMap

case class Genre(code: String, label: String)

object Genre {

  val HOMME = Genre(code = "H", "Homme")
  val FEMME = Genre(code = "F", "Femme")

  private val values = ListMap(
    HOMME.code -> HOMME,
    FEMME.code -> FEMME,
  )

  def from(r: String): Option[Genre] = values.get(r)
}