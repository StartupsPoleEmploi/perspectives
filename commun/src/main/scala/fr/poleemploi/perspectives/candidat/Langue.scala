package fr.poleemploi.perspectives.candidat

import fr.poleemploi.eventsourcing.StringValueObject

case class NiveauLangue(value: String) extends StringValueObject

object NiveauLangue {

  val DEBUTANT = NiveauLangue("1")
  val INTERMEDIAIRE = NiveauLangue("2")
  val COURANT = NiveauLangue("3")
}

case class Langue(label: String,
                  niveau: Option[NiveauLangue])