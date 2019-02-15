package fr.poleemploi.perspectives.offre.domain

import fr.poleemploi.eventsourcing.StringValueObject

case class TypeContrat(value: String) extends StringValueObject

object TypeContrat {

  val CDI = TypeContrat(value = "CDI")
  val CDD = TypeContrat(value = "CDD")
  val INTERIM = TypeContrat(value = "MIS")
  val SAISONNIER = TypeContrat(value = "SAI")

  private val values: Map[String, TypeContrat] = Map(
    CDI.value -> CDI,
    CDD.value -> CDD,
    INTERIM.value -> INTERIM,
    SAISONNIER.value -> SAISONNIER
  )

  def from(value: String): Option[TypeContrat] = values.get(value)
}
