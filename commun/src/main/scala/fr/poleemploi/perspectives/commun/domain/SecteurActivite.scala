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

  val AGRICULTURE = CodeSecteurActivite("A")
  val HOTELLERIE_RESTAURATION = CodeSecteurActivite("G")
  val BATIMENT = CodeSecteurActivite("F")
  val COMMERCE = CodeSecteurActivite("D")
  val SERVICES_A_LA_PERSONNE = CodeSecteurActivite("K")
  val TEXTILE = CodeSecteurActivite("B")
  val INDUSTRIE = CodeSecteurActivite("H")
  val TRANSPORT_LOGISTIQUE = CodeSecteurActivite("N")

  private val values: Map[String, CodeSecteurActivite] = Map(
    AGRICULTURE.value -> AGRICULTURE,
    HOTELLERIE_RESTAURATION.value -> HOTELLERIE_RESTAURATION,
    BATIMENT.value -> BATIMENT,
    COMMERCE.value -> COMMERCE,
    SERVICES_A_LA_PERSONNE.value -> SERVICES_A_LA_PERSONNE,
    TEXTILE.value -> TEXTILE,
    INDUSTRIE.value -> INDUSTRIE,
    TRANSPORT_LOGISTIQUE.value -> TRANSPORT_LOGISTIQUE
  )

  def from(value: String): Option[CodeSecteurActivite] = values.get(value)
}

case class SecteurActivite(code: CodeSecteurActivite,
                           label: String,
                           metiers: List[Metier])

