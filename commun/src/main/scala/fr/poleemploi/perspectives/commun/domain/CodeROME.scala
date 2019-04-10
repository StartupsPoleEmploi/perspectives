package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

/**
  * Value Object permettant d'identifier un métier par son code.
  * ROME : Répertoire Opérationnel des Métiers et des Emplois
  */
case class CodeROME(value: String) extends StringValueObject {

  /**
    * Le code du secteur d'activité d'un métier correspond à la première lettre de son code ROME
    */
  val codeSecteurActivite: CodeSecteurActivite =
    CodeSecteurActivite(value.take(1).toUpperCase)

  /**
    * Le code du domaine professionnel d'un métier correspond aux trois premières lettres de son code ROME
    */
  val codeDomaineProfessionnel: CodeDomaineProfessionnel =
    CodeDomaineProfessionnel(value.take(3).toUpperCase)

}
