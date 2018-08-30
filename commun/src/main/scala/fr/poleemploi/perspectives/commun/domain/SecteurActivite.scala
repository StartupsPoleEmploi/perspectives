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

class SecteurActivite(val code: CodeSecteurActivite,
                      val label: String,
                      val metiers: List[Metier])

object SecteurActivite {
  val AGRICULTURE: SecteurActivite = new SecteurActivite(
    code = CodeSecteurActivite("A"),
    label = "Agriculture",
    metiers = List(
      Metier.AIDE_AGRICOLE
    ))

  val HOTELLERIE_RESTAURATION: SecteurActivite = new SecteurActivite(
    code = CodeSecteurActivite("G"),
    label = "Hôtellerie restauration",
    metiers = List(
      Metier.PERSONNEL_POLYVALENT,
      Metier.SERVICE
    ))

  val BATIMENT: SecteurActivite = new SecteurActivite(
    code = CodeSecteurActivite("F"),
    label = "Bâtiment",
    metiers = List(
      Metier.ELECTRICITE,
      Metier.MACONNERIE,
      Metier.CONDUITE_ENGINS
    ))

  val COMMERCE: SecteurActivite = new SecteurActivite(
    code = CodeSecteurActivite("D"),
    label = "Commerce",
    metiers = List(
      Metier.MISE_EN_RAYON,
      Metier.CAISSE,
      Metier.VENTE
    ))

  val SERVICES_A_LA_PERSONNE: SecteurActivite = new SecteurActivite(
    code = CodeSecteurActivite("K"),
    label = "Services à la personne",
    metiers = List(
      Metier.AIDE_PERSONNES_AGEES,
      Metier.AIDE_DOMICILE,
      Metier.NETTOYAGE_LOCAUX
    ))

  val TEXTILE: SecteurActivite = new SecteurActivite(
    code = CodeSecteurActivite("B"),
    label = "Textile",
    metiers = List(
      Metier.REALISATION_ARTICLES,
      Metier.MECANICIEN_CONFECTION
    ))

  val INDUSTRIE: SecteurActivite = new SecteurActivite(
    code = CodeSecteurActivite("H"),
    label = "Industrie",
    metiers = List(
      Metier.CONDUITE_MACHINE,
      Metier.SOUDAGE,
      Metier.FABRICATION_PIECES,
      Metier.TRI_EMBALLAGE,
      Metier.PREPARATION_COMMANDE
    ))

  val TRANSPORT_LOGISTIQUE: SecteurActivite = new SecteurActivite(
    code = CodeSecteurActivite("N"),
    label = "Transport et logistique",
    metiers = List(
      Metier.MANUTENTION
    ))

  val values = List(
    SecteurActivite.INDUSTRIE,
    SecteurActivite.COMMERCE,
    SecteurActivite.BATIMENT,
    SecteurActivite.HOTELLERIE_RESTAURATION,
    SecteurActivite.AGRICULTURE,
    SecteurActivite.TEXTILE,
    SecteurActivite.SERVICES_A_LA_PERSONNE,
    SecteurActivite.TRANSPORT_LOGISTIQUE
  )

  def parCode(codeSecteurActivite: CodeSecteurActivite): SecteurActivite =
    values
      .find(_.code == codeSecteurActivite)
      .getOrElse(throw new IllegalArgumentException(s"Aucun secteur avec le code $codeSecteurActivite"))

  def parMetier(codeROME: CodeROME): SecteurActivite =
    values
      .find(_.code == CodeSecteurActivite.fromCodeROME(codeROME))
      .getOrElse(throw new IllegalArgumentException(s"Aucun secteur pour le code ROME $codeROME"))
}
