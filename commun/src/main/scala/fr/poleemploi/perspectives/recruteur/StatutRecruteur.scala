package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.eventsourcing.StringValueObject

case class StatutRecruteur(value: String) extends StringValueObject

object StatutRecruteur {

  val NOUVEAU = StatutRecruteur("NOUVEAU")
  val INSCRIT = StatutRecruteur("INSCRIT")
  val PROFIL_COMPLET = StatutRecruteur("PROFIL_COMPLET")
}