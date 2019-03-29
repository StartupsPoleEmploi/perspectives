package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

case class Nom private(value: String) extends StringValueObject

object Nom {

  def apply(nom: String): Nom = new Nom(nom.toLowerCase.split(" ").map(_.capitalize).mkString(" "))
}