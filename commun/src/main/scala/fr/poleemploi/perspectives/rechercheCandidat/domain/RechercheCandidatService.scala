package fr.poleemploi.perspectives.rechercheCandidat.domain

import fr.poleemploi.perspectives.commun.domain._

class RechercheCandidatService {

  def secteursProposes: List[SecteurActivite] =
    RechercheCandidatService.secteurs

  def metierProposeParCode(codeROME: CodeROME): Option[Metier] =
    RechercheCandidatService.metiers.get(codeROME)

  def departementsProposes: List[Departement] =
    RechercheCandidatService.departements

  def departementParCode(code: CodeDepartement): Departement =
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

  val AIDE_AGRICOLE = Metier(
    codeROME = CodeROME("A1401"),
    label = "Aide agricole"
  )
  val REALISATION_ARTICLES = Metier(
    codeROME = CodeROME("B1802"),
    label = "Réalisation d'articles"
  )
  val VENTE = Metier(
    codeROME = CodeROME("D1106"),
    label = "Vente"
  )
  val CAISSE = Metier(
    codeROME = CodeROME("D1505"),
    label = "Caisse"
  )
  val MISE_EN_RAYON = Metier(
    codeROME = CodeROME("D1507"),
    label = "Mise en rayon"
  )
  val CONDUITE_ENGINS = Metier(
    codeROME = CodeROME("F1301"),
    label = "Conduite d’engins"
  )
  val ELECTRICITE = Metier(
    codeROME = CodeROME("F1602"),
    label = "Électricité"
  )
  val MACONNERIE = Metier(
    codeROME = CodeROME("F1703"),
    label = "Maçonnerie"
  )
  val PERSONNEL_POLYVALENT = Metier(
    codeROME = CodeROME("G1603"),
    label = "Personnel polyvalent"
  )
  val SERVICE = Metier(
    codeROME = CodeROME("G1803"),
    label = "Service"
  )
  val CONDUITE_MACHINE_AGRO = Metier(
    codeROME = CodeROME("H2102"),
    label = "Conduite de machines"
  )
  val CONDUITE_MACHINE = Metier(
    codeROME = CodeROME("H2202"),
    label = "Conduite de machines"
  )
  val MECANICIEN_CONFECTION = Metier(
    codeROME = CodeROME("H2402"),
    label = "Mécanicien en confection"
  )
  val SOUDAGE = Metier(
    codeROME = CodeROME("H2913"),
    label = "Soudage"
  )
  val FABRICATION_PIECES = Metier(
    codeROME = CodeROME("H3203"),
    label = "Fabrication de pièces"
  )
  val TRI_EMBALLAGE = Metier(
    codeROME = CodeROME("H3302"),
    label = "Tri et emballage"
  )
  val AIDE_PERSONNES_AGEES = Metier(
    codeROME = CodeROME("K1302"),
    label = "Aide aux personnes âgées"
  )
  val AIDE_DOMICILE = Metier(
    codeROME = CodeROME("K1304"),
    label = "Aide à domicile"
  )
  val NETTOYAGE_LOCAUX = Metier(
    codeROME = CodeROME("K2204"),
    label = "Nettoyage de locaux",
  )
  val PREPARATION_COMMANDE = Metier(
    codeROME = CodeROME("N1103"),
    label = "Préparation de commandes"
  )
  val MANUTENTION = Metier(
    codeROME = CodeROME("N1105"),
    label = "Manutention"
  )
  val CONDUITE_POIDS_LOURDS = Metier(
    codeROME = CodeROME("N4101"),
    label = "Conduite de poids lourds"
  )

  val AGRICULTURE: SecteurActivite = SecteurActivite(
    code = CodeSecteurActivite("A"),
    label = "Agriculture",
    metiers = List(
      AIDE_AGRICOLE
    ))

  val HOTELLERIE_RESTAURATION: SecteurActivite = SecteurActivite(
    code = CodeSecteurActivite("G"),
    label = "Hôtellerie restauration",
    metiers = List(
      PERSONNEL_POLYVALENT,
      SERVICE
    ))

  val BATIMENT: SecteurActivite = SecteurActivite(
    code = CodeSecteurActivite("F"),
    label = "Bâtiment",
    metiers = List(
      ELECTRICITE,
      MACONNERIE,
      CONDUITE_ENGINS
    ))

  val COMMERCE: SecteurActivite = SecteurActivite(
    code = CodeSecteurActivite("D"),
    label = "Commerce",
    metiers = List(
      MISE_EN_RAYON,
      CAISSE,
      VENTE
    ))

  val SERVICES_A_LA_PERSONNE: SecteurActivite = SecteurActivite(
    code = CodeSecteurActivite("K"),
    label = "Services à la personne",
    metiers = List(
      AIDE_PERSONNES_AGEES,
      AIDE_DOMICILE,
      NETTOYAGE_LOCAUX
    ))

  val TEXTILE: SecteurActivite = SecteurActivite(
    code = CodeSecteurActivite("B"),
    label = "Textile",
    metiers = List(
      REALISATION_ARTICLES
    ))

  val INDUSTRIE: SecteurActivite = SecteurActivite(
    code = CodeSecteurActivite("H"),
    label = "Industrie",
    metiers = List(
      CONDUITE_MACHINE,
      SOUDAGE,
      FABRICATION_PIECES,
      TRI_EMBALLAGE,
      MECANICIEN_CONFECTION
    ))

  val TRANSPORT_LOGISTIQUE: SecteurActivite = SecteurActivite(
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
      Departement(
        code = CodeDepartement("85"),
        label = "Vendée"
      )
    )
}
