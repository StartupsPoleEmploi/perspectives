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

  val AGRICULTURE = SecteurActivite(
    value = "A",
    label = "Agriculture",
    metiers = Set(
      Metier.AIDE_AGRICOLE
    ))

  val HOTELLERIE_RESTAURATION = SecteurActivite(
    value = "G",
    label = "Hôtellerie restauration",
    metiers = Set(
      Metier.PERSONNEL_POLYVALENT,
      Metier.SERVICE
    ))

  val BATIMENT = SecteurActivite(
    value = "F",
    label = "Bâtiment",
    metiers = Set(
      Metier.ELECTRICITE,
      Metier.MACONNERIE,
      Metier.CONDUITE_ENGINS
    ))

  val COMMERCE = SecteurActivite(
    value = "D",
    label = "Commerce",
    metiers = Set(
      Metier.MISE_EN_RAYON,
      Metier.CAISSE,
      Metier.VENTE,
      Metier.MANUTENTION,
    ))

  val SERVICES_A_LA_PERSONNE = SecteurActivite(
    value = "K",
    label = "Services à la personne",
    metiers = Set(
      Metier.AIDE_PERSONNES_AGEES,
      Metier.AIDE_DOMICILE,
      Metier.NETTOYAGE_LOCAUX
    ))

  val TEXTILE = SecteurActivite(
    value = "B",
    label = "Textile",
    metiers = Set(
      Metier.REALISATION_ARTICLES,
      Metier.MECANICIEN_CONFECTION
    ))

  val INDUSTRIE = SecteurActivite(
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
}
