package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.ValueObject

/**
  * Value Object RayonRecherche
  */
case class RayonRecherche(value: Int, uniteLongueur: UniteLongueur) extends ValueObject

object RayonRecherche {

  val MAX_10 = RayonRecherche(value = 10, uniteLongueur = UniteLongueur.KM)
  val MAX_30 = RayonRecherche(value = 30, uniteLongueur = UniteLongueur.KM)
  val MAX_50 = RayonRecherche(value = 50, uniteLongueur = UniteLongueur.KM)

  private val values: Map[Int, RayonRecherche] = Map(
    MAX_10.value -> MAX_10,
    MAX_30.value -> MAX_30,
    MAX_50.value -> MAX_50
  )

  def from(value: Int): Option[RayonRecherche] = values.get(value)
}