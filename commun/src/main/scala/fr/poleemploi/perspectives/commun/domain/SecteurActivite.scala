package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

/**
  * Value Object permettant d'identifier un secteur d'activité
  */
case class CodeSecteurActivite(value: String) extends StringValueObject

object CodeSecteurActivite {

  /**
    * Le code du secteur d'activité d'un métier correspond à la première lettre de son code ROME
    */
  def fromCodeROME(codeROME: CodeROME): CodeSecteurActivite =
    CodeSecteurActivite(codeROME.value.take(1).toUpperCase)
}

case class SecteurActivite(code: CodeSecteurActivite,
                           label: String,
                           metiers: List[Metier])

