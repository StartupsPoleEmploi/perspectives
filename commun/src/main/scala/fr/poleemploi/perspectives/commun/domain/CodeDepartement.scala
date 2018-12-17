package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

/**
  * Value Object permettant d'identifier un département
  */
case class CodeDepartement(value: String) extends StringValueObject