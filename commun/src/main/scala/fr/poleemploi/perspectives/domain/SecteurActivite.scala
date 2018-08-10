package fr.poleemploi.perspectives.domain

import fr.poleemploi.eventsourcing.StringValueObject

import scala.collection.immutable.ListMap

/**
  * Value Object SecteurActivite
  */
case class SecteurActivite(value: String,
                           label: String,
                           metiers: Set[Metier]) extends StringValueObject

/**
  * Methodes pour construire et valider un SecteurActivite
  */
object SecteurActivite {

  val AGRICULTURE: SecteurActivite = SecteurActivite(
    value = "A",
    label = "Agriculture",
    metiers = Set(
      Metier.AIDE_AGRICOLE
    ))

  val HOTELLERIE_RESTAURATION: SecteurActivite = SecteurActivite(
    value = "G",
    label = "Hôtellerie restauration",
    metiers = Set(
      Metier.PERSONNEL_POLYVALENT,
      Metier.SERVICE
    ))

  val BATIMENT: SecteurActivite = SecteurActivite(
    value = "F",
    label = "Bâtiment",
    metiers = Set(
      Metier.ELECTRICITE,
      Metier.MACONNERIE,
      Metier.CONDUITE_ENGINS
    ))

  val COMMERCE: SecteurActivite = SecteurActivite(
    value = "D",
    label = "Commerce",
    metiers = Set(
      Metier.MISE_EN_RAYON,
      Metier.CAISSE,
      Metier.VENTE,
      Metier.MANUTENTION,
    ))

  val SERVICES_A_LA_PERSONNE: SecteurActivite = SecteurActivite(
    value = "K",
    label = "Services à la personne",
    metiers = Set(
      Metier.AIDE_PERSONNES_AGEES,
      Metier.AIDE_DOMICILE,
      Metier.NETTOYAGE_LOCAUX
    ))

  val TEXTILE: SecteurActivite = SecteurActivite(
    value = "B",
    label = "Textile",
    metiers = Set(
      Metier.REALISATION_ARTICLES,
      Metier.MECANICIEN_CONFECTION
    ))

  val INDUSTRIE: SecteurActivite = SecteurActivite(
    value = "H",
    label = "Industrie",
    metiers = Set(
      Metier.CONDUITE_MACHINE,
      Metier.SOUDAGE,
      Metier.FABRICATION_PIECES,
      Metier.TRI_EMBALLAGE,
      Metier.PREPARATION_COMMANDE,
      Metier.MANUTENTION
    ))

  val values = ListMap(
    AGRICULTURE.value -> AGRICULTURE,
    HOTELLERIE_RESTAURATION.value -> HOTELLERIE_RESTAURATION,
    BATIMENT.value -> BATIMENT,
    COMMERCE.value -> COMMERCE,
    SERVICES_A_LA_PERSONNE.value -> SERVICES_A_LA_PERSONNE,
    TEXTILE.value -> TEXTILE,
    INDUSTRIE.value -> INDUSTRIE
  )

  def from(value: String): Option[SecteurActivite] = values.get(value)

  def getSecteur(metier: Metier): Option[SecteurActivite] = metier match {
    case Metier.AIDE_AGRICOLE => Some(AGRICULTURE)
    case Metier.CONDUITE_MACHINE_AGRO => Some(AGRICULTURE)
    case Metier.PERSONNEL_POLYVALENT => Some(HOTELLERIE_RESTAURATION)
    case Metier.SERVICE => Some(HOTELLERIE_RESTAURATION)
    case Metier.ELECTRICITE => Some(BATIMENT)
    case Metier.MACONNERIE => Some(BATIMENT)
    case Metier.CONDUITE_ENGINS => Some(BATIMENT)
    case Metier.CAISSE => Some(COMMERCE)
    case Metier.VENTE => Some(COMMERCE)
    case Metier.MISE_EN_RAYON => Some(COMMERCE)
    case Metier.MANUTENTION => Some(COMMERCE)
    case Metier.REALISATION_ARTICLES => Some(TEXTILE)
    case Metier.MECANICIEN_CONFECTION => Some(TEXTILE)
    case Metier.CONDUITE_MACHINE => Some(INDUSTRIE)
    case Metier.SOUDAGE => Some(INDUSTRIE)
    case Metier.FABRICATION_PIECES => Some(INDUSTRIE)
    case Metier.TRI_EMBALLAGE => Some(INDUSTRIE)
    case Metier.DECOUPE_VIANDE => Some(INDUSTRIE)
    case Metier.PREPARATION_COMMANDE => Some(INDUSTRIE)
    case Metier.AIDE_PERSONNES_AGEES => Some(SERVICES_A_LA_PERSONNE)
    case Metier.AIDE_DOMICILE => Some(SERVICES_A_LA_PERSONNE)
    case Metier.NETTOYAGE_LOCAUX => Some(SERVICES_A_LA_PERSONNE)
    case _ => None
  }
}
