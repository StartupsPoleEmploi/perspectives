package fr.poleemploi.perspectives.domain

import fr.poleemploi.eventsourcing.StringValueObject

import scala.collection.immutable.ListMap

/**
  * Value Object Metier
  */
case class Metier(value: String, label: String) extends StringValueObject

/**
  * Methodes pour construire et valider un Metier
  */
object Metier {

  val AIDE_AGRICOLE = Metier("A1401", "Aide agricole")
  val REALISATION_ARTICLES = Metier("B1802", "Réalisation d'articles")
  val VENTE = Metier("D1106", "Vente")
  val CAISSE = Metier("D1505", "Caisse")
  val MISE_EN_RAYON = Metier("D1507", "Mise en rayon")
  val CONDUITE_ENGINS = Metier("F1301", "Conduite d’engins")
  val ELECTRICITE = Metier("F1602", "Électricité")
  val MACONNERIE = Metier("F1703", "Maçonnerie")
  val PERSONNEL_POLYVALENT = Metier("G1603", "Personnel polyvalent")
  val SERVICE = Metier("G1803", "Service")
  val DECOUPE_VIANDE = Metier("H2101", "Découpe de viande")
  val CONDUITE_MACHINE_AGRO = Metier("H2102", "Conduite de machines")
  val CONDUITE_MACHINE = Metier("H2202", "Conduite de machines")
  val MECANICIEN_CONFECTION = Metier("H2402", "Mécanicien en confection")
  val SOUDAGE = Metier("H2913", "Soudage")
  val FABRICATION_PIECES = Metier("H3203", "Fabrication de pièces")
  val TRI_EMBALLAGE = Metier("H3302", "Tri et emballage")
  val AIDE_PERSONNES_AGEES = Metier("K1302", "Aide aux personnes âgées")
  val AIDE_DOMICILE = Metier("K1304", "Aide à domicile")
  val NETTOYAGE_LOCAUX = Metier("K2204", "Nettoyage de locaux")
  val PREPARATION_COMMANDE = Metier("N1103", "Préparation de commandes")
  val MANUTENTION = Metier("N1105", "Manutention")

  val values = ListMap(
    AIDE_AGRICOLE.value -> AIDE_AGRICOLE,
    REALISATION_ARTICLES.value -> REALISATION_ARTICLES,
    CAISSE.value -> CAISSE,
    VENTE.value -> VENTE,
    MISE_EN_RAYON.value -> MISE_EN_RAYON,
    CONDUITE_ENGINS.value -> CONDUITE_ENGINS,
    ELECTRICITE.value -> ELECTRICITE,
    MACONNERIE.value -> MACONNERIE,
    PERSONNEL_POLYVALENT.value -> PERSONNEL_POLYVALENT,
    SERVICE.value -> SERVICE,
    DECOUPE_VIANDE.value -> DECOUPE_VIANDE,
    CONDUITE_MACHINE_AGRO.value -> CONDUITE_MACHINE_AGRO,
    CONDUITE_MACHINE.value -> CONDUITE_MACHINE,
    MECANICIEN_CONFECTION.value -> MECANICIEN_CONFECTION,
    SOUDAGE.value -> SOUDAGE,
    FABRICATION_PIECES.value -> FABRICATION_PIECES,
    TRI_EMBALLAGE.value -> TRI_EMBALLAGE,
    AIDE_PERSONNES_AGEES.value -> AIDE_PERSONNES_AGEES,
    AIDE_DOMICILE.value -> AIDE_DOMICILE,
    NETTOYAGE_LOCAUX.value -> NETTOYAGE_LOCAUX,
    MANUTENTION.value -> MANUTENTION,
    PREPARATION_COMMANDE.value -> PREPARATION_COMMANDE
  )

  def from(code: String): Option[Metier] = values.get(code)

}