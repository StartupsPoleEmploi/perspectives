package fr.poleemploi.perspectives.recruteur.alerte.domain

import fr.poleemploi.eventsourcing.StringValueObject

/**
  * Value Object FrequenceAlerte
  */
case class FrequenceAlerte(value: String) extends StringValueObject

object FrequenceAlerte {

  val QUOTIDIENNE = FrequenceAlerte("QUOTIDIENNE")
  val HEBDOMADAIRE = FrequenceAlerte("HEBDOMADAIRE")

  private val values: Map[String, FrequenceAlerte] = Map(
    QUOTIDIENNE.value -> QUOTIDIENNE,
    HEBDOMADAIRE.value -> HEBDOMADAIRE
  )

  def frequenceAlerte(value: String): Option[FrequenceAlerte] = values.get(value)
}
