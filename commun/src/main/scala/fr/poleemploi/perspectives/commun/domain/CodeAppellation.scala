package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

/**
  * Value Object permettant d'identifier une appellation metier par son code
  * ROME : Répertoire Opérationnel des Métiers et des Emplois
  */
case class CodeAppellation(value: String) extends StringValueObject
