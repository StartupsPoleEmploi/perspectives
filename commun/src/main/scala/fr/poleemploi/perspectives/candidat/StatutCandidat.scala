package fr.poleemploi.perspectives.candidat

import fr.poleemploi.eventsourcing.StringValueObject

case class StatutCandidat(value: String) extends StringValueObject

object StatutCandidat {

  val NOUVEAU = StatutCandidat("NOUVEAU")
  val INSCRIT = StatutCandidat("INSCRIT")
}
