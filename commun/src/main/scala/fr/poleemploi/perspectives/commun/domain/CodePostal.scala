package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

/**
  * Value Object permettant d'identifier un code postal
  */
case class CodePostal(value: String) extends StringValueObject

object CodePostal {

  def from(value: String): Option[CodePostal] =
    if (value.matches("^[0-9]{5}$")) Some(CodePostal(value))
    else None
}
