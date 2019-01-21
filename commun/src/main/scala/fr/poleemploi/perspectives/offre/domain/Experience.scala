package fr.poleemploi.perspectives.offre.domain

import fr.poleemploi.eventsourcing.StringValueObject

case class Experience(value: String) extends StringValueObject

object Experience {

  val DEBUTANT = Experience(value = "DEBUTANT")
}
