package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

/**
  * Value Object Genre
  */
case class Genre(value: String) extends StringValueObject

object Genre {

  val HOMME = Genre(value = "H")
  val FEMME = Genre(value = "F")

  def buildFrom(genre: String): Genre = genre match {
    case "M" => HOMME
    case "F" => FEMME
    case g@_ => throw new IllegalArgumentException(s"Genre inconnu : $g")
  }
}
