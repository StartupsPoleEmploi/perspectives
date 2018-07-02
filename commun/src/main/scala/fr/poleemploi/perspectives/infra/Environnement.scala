package fr.poleemploi.perspectives.infra

import scala.collection.immutable.ListMap

case class Environnement(value: String) {

  override def toString: String = super.toString
}

object Environnement {

  val DEVELOPPEMENT = Environnement(value = "developpement")
  val RECETTE = Environnement(value = "recette")
  val PRODUCTION = Environnement(value = "production")

  private val values = ListMap(
    DEVELOPPEMENT.value -> DEVELOPPEMENT,
    RECETTE.value -> RECETTE,
    PRODUCTION.value -> PRODUCTION
  )

  def from(s: String): Environnement = values.getOrElse(s, throw new IllegalArgumentException(s"Environnement inconnu : $s"))
}