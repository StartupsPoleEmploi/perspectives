package fr.poleemploi.perspectives.domain.recruteur

import fr.poleemploi.eventsourcing.StringValueObject

import scala.collection.immutable.ListMap

/**
  * Value Object TypeRecruteur
  */
case class TypeRecruteur(value: String, label: String) extends StringValueObject

/**
  * Methodes pour construire et valider un TypeRecruteur
  */
object TypeRecruteur {

  val ENTREPRISE = TypeRecruteur("ENTREPRISE", "Entreprise")
  val AGENCE_INTERIM = TypeRecruteur("AGENCE_INTERIM", "Agence d'interim")
  val ORGANISME_FORMATION = TypeRecruteur("ORGANISME_FORMATION", "Organisme de formation")

  val values = ListMap(
    ENTREPRISE.value -> ENTREPRISE,
    AGENCE_INTERIM.value -> AGENCE_INTERIM,
    ORGANISME_FORMATION.value -> ORGANISME_FORMATION
  )

  def from(value: String): Option[TypeRecruteur] = values.get(value)

}
