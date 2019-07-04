package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

case class Email private(value: String) extends StringValueObject

object Email {

  def apply(email: String): Email = new Email(email.toLowerCase)
}