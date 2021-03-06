package fr.poleemploi.perspectives.commun.infra

case class Environnement(value: String)

object Environnement {

  val DEVELOPPEMENT = Environnement(value = "developpement")
  val RECETTE = Environnement(value = "recette")
  val PRODUCTION = Environnement(value = "production")

  private val values: Map[String, Environnement] = Map(
    DEVELOPPEMENT.value -> DEVELOPPEMENT,
    RECETTE.value -> RECETTE,
    PRODUCTION.value -> PRODUCTION
  )

  def from(value: String): Environnement = values.getOrElse(value, throw new IllegalArgumentException(s"Environnement inconnu : $value"))
}