package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

/**
  * Value Object Metier
  */
case class NumeroTelephone(value: String) extends StringValueObject

/**
  * Methodes pour construire et valider un NumeroTelephone
  */
object NumeroTelephone {

  def from(value: String): Option[NumeroTelephone] =
    if (value.matches("^[\\+]?[0-9]+$") && value.length > 0 && value.length <= 11) {
      Some(NumeroTelephone(value))
    } else None

}
