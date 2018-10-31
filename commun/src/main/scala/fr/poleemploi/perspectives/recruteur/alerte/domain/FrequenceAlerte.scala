package fr.poleemploi.perspectives.recruteur.alerte.domain

import fr.poleemploi.eventsourcing.StringValueObject

import scala.collection.immutable.ListMap

/**
  * Value Object FrequenceAlerte
  */
case class FrequenceAlerte(value: String) extends StringValueObject

object FrequenceAlerte {

  val QUOTIDIENNE = FrequenceAlerte("Quotidienne")
  val HEBDOMADAIRE = FrequenceAlerte("Hebdomadaire")

  private val frequenceAlertesParValeur = ListMap(
    QUOTIDIENNE.value -> QUOTIDIENNE,
    HEBDOMADAIRE.value -> HEBDOMADAIRE
  )

  def frequenceAlerte(value: String): Option[FrequenceAlerte] = frequenceAlertesParValeur.get(value)

  def label(frequenceAlerte: FrequenceAlerte): String = frequenceAlerte match {
    case QUOTIDIENNE => "Chaque jour"
    case HEBDOMADAIRE => "Chaque semaine"
    case _ => ""
  }

}
