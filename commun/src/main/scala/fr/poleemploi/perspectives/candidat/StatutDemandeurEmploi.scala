package fr.poleemploi.perspectives.candidat

import fr.poleemploi.eventsourcing.StringValueObject

import scala.collection.immutable.ListMap

/**
  * Value Object StatutDemandeurEmploi
  */
case class StatutDemandeurEmploi(value: String, label: String) extends StringValueObject

/**
  * Methodes pour construire et valider un StatutDemandeurEmploi
  */
object StatutDemandeurEmploi {

  val NON_DEMANDEUR_EMPLOI = StatutDemandeurEmploi(value = "0", "Non demandeur d'emploi")
  val DEMANDEUR_EMPLOI = StatutDemandeurEmploi(value = "1", "Demandeur d'emploi")

  private val values = ListMap(
    NON_DEMANDEUR_EMPLOI.value -> NON_DEMANDEUR_EMPLOI,
    DEMANDEUR_EMPLOI.value -> DEMANDEUR_EMPLOI
  )

  def from(value: String): Option[StatutDemandeurEmploi] = values.get(value)
}
