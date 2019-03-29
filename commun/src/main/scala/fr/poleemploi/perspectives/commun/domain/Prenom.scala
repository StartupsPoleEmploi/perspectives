package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

case class Prenom private(value: String) extends StringValueObject

object Prenom {

  def apply(prenom: String): Prenom = new Prenom(prenom.toLowerCase.split(" ").map(_.capitalize).mkString(" "))
}