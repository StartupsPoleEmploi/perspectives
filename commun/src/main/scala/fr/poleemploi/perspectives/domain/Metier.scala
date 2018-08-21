package fr.poleemploi.perspectives.domain

import fr.poleemploi.eventsourcing.StringValueObject

/**
  * Value Object Metier.
  * La valeur est le code ROME (Répertoire Opérationnel des Métiers et des Emplois)
  */
case class Metier(value: String, label: String, habiletes: List[Habilete] = Nil) extends StringValueObject

/**
  * Methodes pour construire et valider un Metier
  */
object Metier {

  // FIXME : récupérer les habiletés pour tous les métiers proposés
  val AIDE_AGRICOLE = Metier(
    value = "A1401",
    label = "Aide agricole",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )
  val REALISATION_ARTICLES = Metier(
    value = "B1802",
    label = "Réalisation d'articles",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.REPRESENTATION_ESPACE,
      Habilete.DEXTERITE,
      Habilete.MAINTIENT_ATTENTION,
      Habilete.REPRESENTATION_PROCESSUS
    )
  )
  val VENTE = Metier(
    value = "D1106",
    label = "Vente",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )
  val CAISSE = Metier(
    value = "D1505",
    label = "Caisse",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )
  val MISE_EN_RAYON = Metier(
    value = "D1507",
    label = "Mise en rayon",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.MAINTIENT_ATTENTION,
      Habilete.ORGANISER,
      Habilete.REPRESENTATION_ESPACE,
      Habilete.TRAVAIL_EN_EQUIPE
    )
  )
  val CONDUITE_ENGINS = Metier(
    value = "F1301",
    label = "Conduite d’engins",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )
  val ELECTRICITE = Metier(
    value = "F1602",
    label = "Électricité",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )
  val MACONNERIE = Metier(
    value = "F1703",
    label = "Maçonnerie",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )
  val PERSONNEL_POLYVALENT = Metier(
    value = "G1603",
    label = "Personnel polyvalent",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.MAINTIENT_ATTENTION,
      Habilete.RELATION_SERVICE,
      Habilete.TRAVAIL_EN_EQUIPE
    )
  )
  val SERVICE = Metier(
    value = "G1803",
    label = "Service",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )
  val DECOUPE_VIANDE = Metier(
    value = "H2101",
    label = "Découpe de viande",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )
  val CONDUITE_MACHINE_AGRO = Metier(
    value = "H2102",
    label = "Conduite de machines",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.MAINTIENT_ATTENTION,
      Habilete.DEXTERITE,
      Habilete.REPRESENTATION_PROCESSUS
    )
  )
  val CONDUITE_MACHINE = Metier(
    value = "H2202",
    label = "Conduite de machines",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.MAINTIENT_ATTENTION,
      Habilete.DEXTERITE,
      Habilete.REPRESENTATION_PROCESSUS
    )
  )
  val MECANICIEN_CONFECTION = Metier(
    value = "H2402",
    label = "Mécanicien en confection",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.MAINTIENT_ATTENTION,
      Habilete.DEXTERITE,
      Habilete.REPRESENTATION_ESPACE,
      Habilete.ADAPTATION
    )
  )
  val SOUDAGE = Metier(
    value = "H2913",
    label = "Soudage",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.MAINTIENT_ATTENTION,
      Habilete.DEXTERITE,
      Habilete.REPRESENTATION_ESPACE
    )
  )
  val FABRICATION_PIECES = Metier(
    value = "H3203",
    label = "Fabrication de pièces",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.MAINTIENT_ATTENTION,
      Habilete.ORGANISER,
      Habilete.RECEUIL_ANALYSE_DONNEES,
      Habilete.TRAVAIL_SOUS_TENSION
    )
  )
  val TRI_EMBALLAGE = Metier(
    value = "H3302",
    label = "Tri et emballage",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.MAINTIENT_ATTENTION,
      Habilete.TRAVAIL_EN_EQUIPE,
      Habilete.REPRESENTATION_ESPACE,
      Habilete.REPRESENTATION_PROCESSUS
    )
  )
  val AIDE_PERSONNES_AGEES = Metier(
    value = "K1302",
    label = "Aide aux personnes âgées",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )
  val AIDE_DOMICILE = Metier(
    value = "K1304",
    label = "Aide à domicile",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )
  val NETTOYAGE_LOCAUX = Metier(
    value = "K2204",
    label = "Nettoyage de locaux",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.MAINTIENT_ATTENTION,
      Habilete.RELATION_SERVICE,
      Habilete.PRISE_D_INITIATIVES
    )
  )
  val PREPARATION_COMMANDE = Metier(
    value = "N1103",
    label = "Préparation de commandes",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES,
      Habilete.TRAVAIL_SOUS_TENSION,
      Habilete.PRISE_D_INITIATIVES
    )
  )
  val MANUTENTION = Metier(
    value = "N1105",
    label = "Manutention",
    habiletes = List(
      Habilete.RESPECT_NORMES_ET_CONSIGNES
    )
  )

  val values = Map(
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

  def from(value: String): Option[Metier] = values.get(value)

}