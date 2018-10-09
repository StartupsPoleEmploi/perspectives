package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

/**
  * Value Object Genre
  */
case class Genre(value: String) extends StringValueObject

object Genre {

  val HOMME = Genre(value = "H")
  val FEMME = Genre(value = "F")

  def label(genre: Genre): String =
    genre match {
      case HOMME => "Homme"
      case FEMME => "Femme"
      case _ => ""
    }
}