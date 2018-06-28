package fr.poleemploi.perspectives.domain

import scala.collection.immutable.ListMap

/**
  * Value Object Metier
  */
case class Metier(code: String, label: String)

/**
  * Factory methods pour construire et valider un Metier
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
    AIDE_AGRICOLE.code -> AIDE_AGRICOLE,
    REALISATION_ARTICLES.code -> REALISATION_ARTICLES,
    CAISSE.code -> CAISSE,
    VENTE.code -> VENTE,
    MISE_EN_RAYON.code -> MISE_EN_RAYON,
    CONDUITE_ENGINS.code -> CONDUITE_ENGINS,
    ELECTRICITE.code -> ELECTRICITE,
    MACONNERIE.code -> MACONNERIE,
    PERSONNEL_POLYVALENT.code -> PERSONNEL_POLYVALENT,
    SERVICE.code -> SERVICE,
    DECOUPE_VIANDE.code -> DECOUPE_VIANDE,
    CONDUITE_MACHINE_AGRO.code -> CONDUITE_MACHINE_AGRO,
    CONDUITE_MACHINE.code -> CONDUITE_MACHINE,
    MECANICIEN_CONFECTION.code -> MECANICIEN_CONFECTION,
    SOUDAGE.code -> SOUDAGE,
    FABRICATION_PIECES.code -> FABRICATION_PIECES,
    TRI_EMBALLAGE.code -> TRI_EMBALLAGE,
    AIDE_PERSONNES_AGEES.code -> AIDE_PERSONNES_AGEES,
    AIDE_DOMICILE.code -> AIDE_DOMICILE,
    NETTOYAGE_LOCAUX.code -> NETTOYAGE_LOCAUX,
    MANUTENTION.code -> MANUTENTION,
    PREPARATION_COMMANDE.code -> PREPARATION_COMMANDE
  )

  def from(code: String): Option[Metier] = values.get(code)

}