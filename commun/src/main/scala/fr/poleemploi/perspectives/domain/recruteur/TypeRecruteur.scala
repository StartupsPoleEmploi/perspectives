package fr.poleemploi.perspectives.domain.recruteur

import scala.collection.immutable.ListMap

/**
  * Value Object TypeRecruteur
  */
case class TypeRecruteur(code: String, value: String)

/**
  * Factory methods pour construire et valider un TypeRecruteur
  */
object TypeRecruteur {

  val ENTREPRISE = TypeRecruteur("ENTREPRISE", "Entreprise")
  val AGENCE_INTERIM = TypeRecruteur("AGENCE_INTERIM", "Agence d'interim")
  val ORGANISME_FORMATION = TypeRecruteur("ORGANISME_FORMATION", "Organisme de formation")

  val values = ListMap(
    ENTREPRISE.code -> ENTREPRISE,
    AGENCE_INTERIM.code -> AGENCE_INTERIM,
    ORGANISME_FORMATION.code -> ORGANISME_FORMATION
  )

  def from(code: String): Option[TypeRecruteur] = values.get(code)

}
