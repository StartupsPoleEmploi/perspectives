package fr.poleemploi.perspectives.metier.domain

import fr.poleemploi.perspectives.commun.domain._

trait ReferentielMetier {

  def secteursProposesPourRecherche: List[SecteurActivite] =
    ReferentielMetier.secteursProposesPourRecherche

  def metierProposePourRechercheParCode(codeROME: CodeROME): Option[Metier] =
    ReferentielMetier.metiersProposesPourRecherche.get(codeROME)

  def secteurActiviteParCode(codeSecteurActivite: CodeSecteurActivite): SecteurActivite =
    secteursProposesPourRecherche
      .find(_.code == codeSecteurActivite)
      .getOrElse(throw new IllegalArgumentException(s"Aucun secteur avec le code $codeSecteurActivite"))

  def secteurActivitePourCodeROME(codeROME: CodeROME): SecteurActivite =
    secteursProposesPourRecherche
      .find(_.code == CodeSecteurActivite.fromCodeROME(codeROME))
      .getOrElse(throw new IllegalArgumentException(s"Aucun secteur pour le code ROME $codeROME"))

  def metierParCode(code: CodeROME): Metier
}

object ReferentielMetier {

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

  val AGRICULTURE: SecteurActivite = new SecteurActivite(
    code = CodeSecteurActivite("A"),
    label = "Agriculture",
    metiers = List(
      AIDE_AGRICOLE
    ))

  val HOTELLERIE_RESTAURATION: SecteurActivite = new SecteurActivite(
    code = CodeSecteurActivite("G"),
    label = "Hôtellerie restauration",
    metiers = List(
      PERSONNEL_POLYVALENT,
      SERVICE
    ))

  val BATIMENT: SecteurActivite = new SecteurActivite(
    code = CodeSecteurActivite("F"),
    label = "Bâtiment",
    metiers = List(
      ELECTRICITE,
      MACONNERIE,
      CONDUITE_ENGINS
    ))

  val COMMERCE: SecteurActivite = new SecteurActivite(
    code = CodeSecteurActivite("D"),
    label = "Commerce",
    metiers = List(
      MISE_EN_RAYON,
      CAISSE,
      VENTE
    ))

  val SERVICES_A_LA_PERSONNE: SecteurActivite = new SecteurActivite(
    code = CodeSecteurActivite("K"),
    label = "Services à la personne",
    metiers = List(
      AIDE_PERSONNES_AGEES,
      AIDE_DOMICILE,
      NETTOYAGE_LOCAUX
    ))

  val TEXTILE: SecteurActivite = new SecteurActivite(
    code = CodeSecteurActivite("B"),
    label = "Textile",
    metiers = List(
      REALISATION_ARTICLES,
      MECANICIEN_CONFECTION
    ))

  val INDUSTRIE: SecteurActivite = new SecteurActivite(
    code = CodeSecteurActivite("H"),
    label = "Industrie",
    metiers = List(
      CONDUITE_MACHINE,
      SOUDAGE,
      FABRICATION_PIECES,
      TRI_EMBALLAGE,
      PREPARATION_COMMANDE
    ))

  val TRANSPORT_LOGISTIQUE: SecteurActivite = new SecteurActivite(
    code = CodeSecteurActivite("N"),
    label = "Transport et logistique",
    metiers = List(
      MANUTENTION
    ))

  val secteursProposesPourRecherche = List(
    INDUSTRIE,
    COMMERCE,
    BATIMENT,
    HOTELLERIE_RESTAURATION,
    AGRICULTURE,
    TEXTILE,
    SERVICES_A_LA_PERSONNE,
    TRANSPORT_LOGISTIQUE
  )

  val metiersProposesPourRecherche: Map[CodeROME, Metier] =
    secteursProposesPourRecherche
      .flatMap(_.metiers)
      .foldLeft(Map[CodeROME, Metier]())(
        (map, metier) => map + (metier.codeROME -> metier)
      )
}

