package fr.poleemploi.perspectives.rechercheCandidat.domain

import fr.poleemploi.perspectives.commun.domain._

class RechercheCandidatService {

  def secteursProposes: List[SecteurActivite] =
    RechercheCandidatService.secteurs

  def metierProposeParCode(codeROME: CodeROME): Option[Metier] =
    RechercheCandidatService.metiers.get(codeROME)

  def departementsProposes: List[Departement] =
    RechercheCandidatService.departements

  def departementParCode(code: String): Departement =
    departementsProposes
      .find(_.code == code)
      .getOrElse(throw new IllegalArgumentException(s"Aucun département avec le code $code"))

  def secteurActiviteParCode(codeSecteurActivite: CodeSecteurActivite): SecteurActivite =
    secteursProposes
      .find(_.code == codeSecteurActivite)
      .getOrElse(throw new IllegalArgumentException(s"Aucun secteur avec le code $codeSecteurActivite"))

  def secteurActivitePourCodeROME(codeROME: CodeROME): SecteurActivite =
    secteursProposes
      .find(_.code == CodeSecteurActivite.fromCodeROME(codeROME))
      .getOrElse(throw new IllegalArgumentException(s"Aucun secteur pour le code ROME $codeROME"))
}

object RechercheCandidatService {

  val AIDE_AGRICOLE = new Metier(
    codeROME = CodeROME("A1401"),
    label = "Aide agricole"
  )
  val REALISATION_ARTICLES = new Metier(
    codeROME = CodeROME("B1802"),
    label = "Réalisation d'articles"
  )
  val VENTE = new Metier(
    codeROME = CodeROME("D1106"),
    label = "Vente"
  )
  val CAISSE = new Metier(
    codeROME = CodeROME("D1505"),
    label = "Caisse"
  )
  val MISE_EN_RAYON = new Metier(
    codeROME = CodeROME("D1507"),
    label = "Mise en rayon"
  )
  val CONDUITE_ENGINS = new Metier(
    codeROME = CodeROME("F1301"),
    label = "Conduite d’engins"
  )
  val ELECTRICITE = new Metier(
    codeROME = CodeROME("F1602"),
    label = "Électricité"
  )
  val MACONNERIE = new Metier(
    codeROME = CodeROME("F1703"),
    label = "Maçonnerie"
  )
  val PERSONNEL_POLYVALENT = new Metier(
    codeROME = CodeROME("G1603"),
    label = "Personnel polyvalent"
  )
  val SERVICE = new Metier(
    codeROME = CodeROME("G1803"),
    label = "Service"
  )
  val CONDUITE_MACHINE_AGRO = new Metier(
    codeROME = CodeROME("H2102"),
    label = "Conduite de machines"
  )
  val CONDUITE_MACHINE = new Metier(
    codeROME = CodeROME("H2202"),
    label = "Conduite de machines"
  )
  val MECANICIEN_CONFECTION = new Metier(
    codeROME = CodeROME("H2402"),
    label = "Mécanicien en confection"
  )
  val SOUDAGE = new Metier(
    codeROME = CodeROME("H2913"),
    label = "Soudage"
  )
  val FABRICATION_PIECES = new Metier(
    codeROME = CodeROME("H3203"),
    label = "Fabrication de pièces"
  )
  val TRI_EMBALLAGE = new Metier(
    codeROME = CodeROME("H3302"),
    label = "Tri et emballage"
  )
  val AIDE_PERSONNES_AGEES = new Metier(
    codeROME = CodeROME("K1302"),
    label = "Aide aux personnes âgées"
  )
  val AIDE_DOMICILE = new Metier(
    codeROME = CodeROME("K1304"),
    label = "Aide à domicile"
  )
  val NETTOYAGE_LOCAUX = new Metier(
    codeROME = CodeROME("K2204"),
    label = "Nettoyage de locaux",
  )
  val PREPARATION_COMMANDE = new Metier(
    codeROME = CodeROME("N1103"),
    label = "Préparation de commandes"
  )
  val MANUTENTION = new Metier(
    codeROME = CodeROME("N1105"),
    label = "Manutention"
  )
  val CONDUITE_POIDS_LOURDS = new Metier(
    codeROME = CodeROME("N4101"),
    label = "Conduite de poids lourds"
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
      REALISATION_ARTICLES
    ))

  val INDUSTRIE: SecteurActivite = new SecteurActivite(
    code = CodeSecteurActivite("H"),
    label = "Industrie",
    metiers = List(
      CONDUITE_MACHINE,
      SOUDAGE,
      FABRICATION_PIECES,
      TRI_EMBALLAGE,
      MECANICIEN_CONFECTION
    ))

  val TRANSPORT_LOGISTIQUE: SecteurActivite = new SecteurActivite(
    code = CodeSecteurActivite("N"),
    label = "Transport et logistique",
    metiers = List(
      MANUTENTION,
      PREPARATION_COMMANDE,
      CONDUITE_POIDS_LOURDS
    ))

  private val secteurs =
    List(
      INDUSTRIE,
      COMMERCE,
      BATIMENT,
      HOTELLERIE_RESTAURATION,
      AGRICULTURE,
      TEXTILE,
      SERVICES_A_LA_PERSONNE,
      TRANSPORT_LOGISTIQUE
    )

  private val metiers: Map[CodeROME, Metier] =
    secteurs
      .flatMap(_.metiers)
      .foldLeft(Map[CodeROME, Metier]())(
        (map, metier) => map + (metier.codeROME -> metier)
      )

  private val departements: List[Departement] =
    List(
      new Departement(
        code = "85",
        label = "Vendée"
      )
    )
}
