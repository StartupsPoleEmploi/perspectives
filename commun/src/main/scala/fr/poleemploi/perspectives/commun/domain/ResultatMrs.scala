package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

/**
  * Value Object ResultatMrs
  */
case class ResultatMrs(value: String) extends StringValueObject

object ResultatMrs {

  val VSL: ResultatMrs = ResultatMrs(value = "VSL")
  val VEM: ResultatMrs = ResultatMrs(value = "VEM") // embauche
  val VEF: ResultatMrs = ResultatMrs(value = "VEF") // entree en formation

  def buildFrom(resultatMrs: String): ResultatMrs = resultatMrs match {
    case "VSL" => VSL
    case "VEM" => VEM
    case "VEF" => VEF
    case g@_ => throw new IllegalArgumentException(s"Resultat MRS inconnu : $g")
  }
}
