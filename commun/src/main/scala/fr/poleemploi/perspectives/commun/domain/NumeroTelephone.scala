package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

/**
  * Value Object NumeroTelephone
  */
case class NumeroTelephone(value: String) extends StringValueObject

object NumeroTelephone {

  def from(value: String): Option[NumeroTelephone] =
    if (value.matches("^[\\+]?[0-9]+$") && value.length >= 10 && value.length <= 11) {
      Some(NumeroTelephone(value))
    } else None

}
