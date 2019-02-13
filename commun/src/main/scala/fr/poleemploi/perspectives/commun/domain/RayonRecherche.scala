package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.IntValueObject

/**
  * Value Object RayonRecherche.
  * La valeur est en Kilomètre
  */
case class RayonRecherche(value: Int) extends IntValueObject

object RayonRecherche {

  val MAX_10 = RayonRecherche(value = 10)
  val MAX_30 = RayonRecherche(value = 30)
  val MAX_50 = RayonRecherche(value = 50)
  val MAX_100 = RayonRecherche(value = 100)

  private val values: Map[Int, RayonRecherche] = Map(
    MAX_10.value -> MAX_10,
    MAX_30.value -> MAX_30,
    MAX_50.value -> MAX_50,
    MAX_100.value -> MAX_100
  )

  def from(value: Int): Option[RayonRecherche] = values.get(value)
}


