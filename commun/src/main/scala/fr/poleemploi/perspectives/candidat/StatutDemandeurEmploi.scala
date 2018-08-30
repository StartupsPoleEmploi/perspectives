package fr.poleemploi.perspectives.candidat

import fr.poleemploi.eventsourcing.StringValueObject

/**
  * Value Object StatutDemandeurEmploi
  */
case class StatutDemandeurEmploi(value: String) extends StringValueObject

object StatutDemandeurEmploi {

  val NON_DEMANDEUR_EMPLOI = StatutDemandeurEmploi(value = "0")
  val DEMANDEUR_EMPLOI = StatutDemandeurEmploi(value = "1")

  def getLabel(statutDemandeurEmploi: StatutDemandeurEmploi): String =
    statutDemandeurEmploi match {
      case NON_DEMANDEUR_EMPLOI => "Non demandeur d'emploi"
      case DEMANDEUR_EMPLOI => "Demandeur d'emploi"
      case _ => ""
    }
}