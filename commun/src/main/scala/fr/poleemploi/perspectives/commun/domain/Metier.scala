package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

/**
  * Value Object permettant d'identifier un métier par son code.
  * ROME : Répertoire Opérationnel des Métiers et des Emplois
  */
case class CodeROME(value: String) extends StringValueObject

class Metier(val codeROME: CodeROME,
             val label: String)