package fr.poleemploi.perspectives.candidat

import fr.poleemploi.eventsourcing.StringValueObject

case class NiveauSavoirFaire(value: String) extends StringValueObject

object NiveauSavoirFaire {

  val DEBUTANT = NiveauSavoirFaire("1")
  val INTERMEDIAIRE = NiveauSavoirFaire("2")
  val AVANCE = NiveauSavoirFaire("3")
}

case class SavoirFaire(label: String,
                       niveau: Option[NiveauSavoirFaire])