package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

/**
  * Value Object permettant d'identifier un domaine professionnel
  */
case class CodeDomaineProfessionnel(value: String) extends StringValueObject
