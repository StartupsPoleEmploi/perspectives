package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.eventsourcing.StringValueObject

/**
  * Value Object TypeRecruteur
  */
case class TypeRecruteur(value: String) extends StringValueObject

object TypeRecruteur {

  val ENTREPRISE = TypeRecruteur("ENTREPRISE")
  val AGENCE_INTERIM = TypeRecruteur("AGENCE_INTERIM")
  val ORGANISME_FORMATION = TypeRecruteur("ORGANISME_FORMATION")

  val values = List(
    ENTREPRISE,
    AGENCE_INTERIM,
    ORGANISME_FORMATION
  )

  private val typeRecruteurParValue = Map(
    ENTREPRISE.value -> ENTREPRISE,
    AGENCE_INTERIM.value -> AGENCE_INTERIM,
    ORGANISME_FORMATION.value -> ORGANISME_FORMATION
  )

  def from(value: String): Option[TypeRecruteur] = typeRecruteurParValue.get(value)
}
