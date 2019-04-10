package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

/**
  * Value Object permettant d'identifier un secteur d'activité
  */
case class CodeSecteurActivite(value: String) extends StringValueObject
