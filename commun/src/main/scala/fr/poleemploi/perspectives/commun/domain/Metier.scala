package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

/**
  * Value Object permettant d'identifier un métier par son code.
  * ROME : Répertoire Opérationnel des Métiers et des Emplois
  */
case class CodeROME(value: String) extends StringValueObject

class Metier(val codeROME: CodeROME,
             val label: String,
             val habiletes: List[Habilete])

object Metier {

  // FIXME : récupérer les habiletés pour tous les métiers proposés
  val AIDE_AGRICOLE = new Metier(
    codeROME = CodeROME("A1401"),
    label = "Aide agricole",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )
  val REALISATION_ARTICLES = new Metier(
    codeROME = CodeROME("B1802"),
    label = "Réalisation d'articles",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.REPRESENTATION_ESPACE,
      Habilete.DEXTERITE,
      Habilete.MAINTIENT_ATTENTION,
      Habilete.REPRESENTATION_PROCESSUS
    )
  )
  val VENTE = new Metier(
    codeROME = CodeROME("D1106"),
    label = "Vente",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )
  val CAISSE = new Metier(
    codeROME = CodeROME("D1505"),
    label = "Caisse",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )
  val MISE_EN_RAYON = new Metier(
    codeROME = CodeROME("D1507"),
    label = "Mise en rayon",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.MAINTIENT_ATTENTION,
      Habilete.ORGANISER,
      Habilete.REPRESENTATION_ESPACE,
      Habilete.TRAVAIL_EN_EQUIPE
    )
  )
  val CONDUITE_ENGINS = new Metier(
    codeROME = CodeROME("F1301"),
    label = "Conduite d’engins",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )
  val ELECTRICITE = new Metier(
    codeROME = CodeROME("F1602"),
    label = "Électricité",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )
  val MACONNERIE = new Metier(
    codeROME = CodeROME("F1703"),
    label = "Maçonnerie",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )
  val PERSONNEL_POLYVALENT = new Metier(
    codeROME = CodeROME("G1603"),
    label = "Personnel polyvalent",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.MAINTIENT_ATTENTION,
      Habilete.RELATION_SERVICE,
      Habilete.TRAVAIL_EN_EQUIPE
    )
  )
  val SERVICE = new Metier(
    codeROME = CodeROME("G1803"),
    label = "Service",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )
  val DECOUPE_VIANDE = new Metier(
    codeROME = CodeROME("H2101"),
    label = "Découpe de viande",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )
  val CONDUITE_MACHINE_AGRO = new Metier(
    codeROME = CodeROME("H2102"),
    label = "Conduite de machines",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.MAINTIENT_ATTENTION,
      Habilete.DEXTERITE,
      Habilete.REPRESENTATION_PROCESSUS
    )
  )
  val CONDUITE_MACHINE = new Metier(
    codeROME = CodeROME("H2202"),
    label = "Conduite de machines",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.MAINTIENT_ATTENTION,
      Habilete.DEXTERITE,
      Habilete.REPRESENTATION_PROCESSUS
    )
  )
  val MECANICIEN_CONFECTION = new Metier(
    codeROME = CodeROME("H2402"),
    label = "Mécanicien en confection",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.MAINTIENT_ATTENTION,
      Habilete.DEXTERITE,
      Habilete.REPRESENTATION_ESPACE,
      Habilete.ADAPTATION
    )
  )
  val SOUDAGE = new Metier(
    codeROME = CodeROME("H2913"),
    label = "Soudage",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.MAINTIENT_ATTENTION,
      Habilete.DEXTERITE,
      Habilete.REPRESENTATION_ESPACE
    )
  )
  val FABRICATION_PIECES = new Metier(
    codeROME = CodeROME("H3203"),
    label = "Fabrication de pièces",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.MAINTIENT_ATTENTION,
      Habilete.ORGANISER,
      Habilete.RECEUIL_ANALYSE_DONNEES,
      Habilete.TRAVAIL_SOUS_TENSION
    )
  )
  val TRI_EMBALLAGE = new Metier(
    codeROME = CodeROME("H3302"),
    label = "Tri et emballage",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.MAINTIENT_ATTENTION,
      Habilete.TRAVAIL_EN_EQUIPE,
      Habilete.REPRESENTATION_ESPACE,
      Habilete.REPRESENTATION_PROCESSUS
    )
  )
  val AIDE_PERSONNES_AGEES = new Metier(
    codeROME = CodeROME("K1302"),
    label = "Aide aux personnes âgées",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )
  val AIDE_DOMICILE = new Metier(
    codeROME = CodeROME("K1304"),
    label = "Aide à domicile",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )
  val NETTOYAGE_LOCAUX = new Metier(
    codeROME = CodeROME("K2204"),
    label = "Nettoyage de locaux",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.MAINTIENT_ATTENTION,
      Habilete.RELATION_SERVICE,
      Habilete.PRISE_D_INITIATIVES
    )
  )
  val PREPARATION_COMMANDE = new Metier(
    codeROME = CodeROME("N1103"),
    label = "Préparation de commandes",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.TRAVAIL_SOUS_TENSION,
      Habilete.PRISE_D_INITIATIVES
    )
  )
  val MANUTENTION = new Metier(
    codeROME = CodeROME("N1105"),
    label = "Manutention",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )

  val values = Map(
    AIDE_AGRICOLE.codeROME -> AIDE_AGRICOLE,
    REALISATION_ARTICLES.codeROME -> REALISATION_ARTICLES,
    CAISSE.codeROME -> CAISSE,
    VENTE.codeROME -> VENTE,
    MISE_EN_RAYON.codeROME -> MISE_EN_RAYON,
    CONDUITE_ENGINS.codeROME -> CONDUITE_ENGINS,
    ELECTRICITE.codeROME -> ELECTRICITE,
    MACONNERIE.codeROME -> MACONNERIE,
    PERSONNEL_POLYVALENT.codeROME -> PERSONNEL_POLYVALENT,
    SERVICE.codeROME -> SERVICE,
    DECOUPE_VIANDE.codeROME -> DECOUPE_VIANDE,
    CONDUITE_MACHINE_AGRO.codeROME -> CONDUITE_MACHINE_AGRO,
    CONDUITE_MACHINE.codeROME -> CONDUITE_MACHINE,
    MECANICIEN_CONFECTION.codeROME -> MECANICIEN_CONFECTION,
    SOUDAGE.codeROME -> SOUDAGE,
    FABRICATION_PIECES.codeROME -> FABRICATION_PIECES,
    TRI_EMBALLAGE.codeROME -> TRI_EMBALLAGE,
    AIDE_PERSONNES_AGEES.codeROME -> AIDE_PERSONNES_AGEES,
    AIDE_DOMICILE.codeROME -> AIDE_DOMICILE,
    NETTOYAGE_LOCAUX.codeROME -> NETTOYAGE_LOCAUX,
    MANUTENTION.codeROME -> MANUTENTION,
    PREPARATION_COMMANDE.codeROME -> PREPARATION_COMMANDE
  )

  def from(codeROME: CodeROME): Option[Metier] = values.get(codeROME)
}