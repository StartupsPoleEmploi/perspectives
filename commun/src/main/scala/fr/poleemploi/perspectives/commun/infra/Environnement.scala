package fr.poleemploi.perspectives.commun.infra

import scala.collection.immutable.ListMap

case class Environnement(value: String)

object Environnement {

  val DEVELOPPEMENT = Environnement(value = "developpement")
  val RECETTE = Environnement(value = "recette")
  val PRODUCTION = Environnement(value = "production")

  private val values = ListMap(
    DEVELOPPEMENT.value -> DEVELOPPEMENT,
    RECETTE.value -> RECETTE,
    PRODUCTION.value -> PRODUCTION
  )

  def from(value: String): Environnement = values.getOrElse(value, throw new IllegalArgumentException(s"Environnement inconnu : $value"))
}