package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

case class CodeSafir(value: String) extends StringValueObject

object CodeSafir {

  def from(value: String): Option[CodeSafir] =
    if (value.matches("^[0-9]{5}$")) Some(CodeSafir(value))
    else None
}
