package fr.poleemploi.perspectives.domain

import fr.poleemploi.eventsourcing.StringValueObject

import scala.collection.immutable.ListMap

/**
  * Value Object SecteurActivite
  */
case class SecteurActivite(value: String,
                           label: String,
                           metiers: List[Metier]) extends StringValueObject

/**
  * Methodes pour construire et valider un SecteurActivite
  */
object SecteurActivite {

  val AGRICULTURE: SecteurActivite = SecteurActivite(
    value = "A",
    label = "Agriculture",
    metiers = List(
      Metier.AIDE_AGRICOLE
    ))

  val HOTELLERIE_RESTAURATION: SecteurActivite = SecteurActivite(
    value = "G",
    label = "Hôtellerie restauration",
    metiers = List(
      Metier.PERSONNEL_POLYVALENT,
      Metier.SERVICE
    ))

  val BATIMENT: SecteurActivite = SecteurActivite(
    value = "F",
    label = "Bâtiment",
    metiers = List(
      Metier.ELECTRICITE,
      Metier.MACONNERIE,
      Metier.CONDUITE_ENGINS
    ))

  val COMMERCE: SecteurActivite = SecteurActivite(
    value = "D",
    label = "Commerce",
    metiers = List(
      Metier.MISE_EN_RAYON,
      Metier.CAISSE,
      Metier.VENTE
    ))

  val SERVICES_A_LA_PERSONNE: SecteurActivite = SecteurActivite(
    value = "K",
    label = "Services à la personne",
    metiers = List(
      Metier.AIDE_PERSONNES_AGEES,
      Metier.AIDE_DOMICILE,
      Metier.NETTOYAGE_LOCAUX
    ))

  val TEXTILE: SecteurActivite = SecteurActivite(
    value = "B",
    label = "Textile",
    metiers = List(
      Metier.REALISATION_ARTICLES,
      Metier.MECANICIEN_CONFECTION
    ))

  val INDUSTRIE: SecteurActivite = SecteurActivite(
    value = "H",
    label = "Industrie",
    metiers = List(
      Metier.CONDUITE_MACHINE,
      Metier.SOUDAGE,
      Metier.FABRICATION_PIECES,
      Metier.TRI_EMBALLAGE,
      Metier.PREPARATION_COMMANDE
    ))

  val TRANSPORT_LOGISTIQUE: SecteurActivite = SecteurActivite(
    value = "N",
    label = "Transport et logistique",
    metiers = List(
      Metier.MANUTENTION
    ))

  val values = ListMap(
    AGRICULTURE.value -> AGRICULTURE,
    HOTELLERIE_RESTAURATION.value -> HOTELLERIE_RESTAURATION,
    BATIMENT.value -> BATIMENT,
    COMMERCE.value -> COMMERCE,
    SERVICES_A_LA_PERSONNE.value -> SERVICES_A_LA_PERSONNE,
    TEXTILE.value -> TEXTILE,
    INDUSTRIE.value -> INDUSTRIE,
    TRANSPORT_LOGISTIQUE.value -> TRANSPORT_LOGISTIQUE
  )

  def from(value: String): Option[SecteurActivite] = values.get(value)

  def getSecteur(metier: Metier): Option[SecteurActivite] = values.get(metier.value.take(1).toUpperCase)
}
