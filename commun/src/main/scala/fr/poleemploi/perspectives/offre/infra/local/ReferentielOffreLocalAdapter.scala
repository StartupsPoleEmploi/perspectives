package fr.poleemploi.perspectives.offre.infra.local

import java.time.LocalDateTime
import java.util.UUID

import fr.poleemploi.perspectives.commun.domain.{CodeROME, Email, Metier, NumeroTelephone}
import fr.poleemploi.perspectives.offre.domain.{CriteresRechercheOffre, Offre, OffreId, ReferentielOffre}

import scala.concurrent.Future

class ReferentielOffreLocalAdapter extends ReferentielOffre {

  override def rechercherOffres(criteres: CriteresRechercheOffre): Future[List[Offre]] = Future.successful(List.tabulate(40)(n =>
    if (n % 3 == 0)
      Offre(
        id = OffreId(UUID.randomUUID().toString),
        intitule = "Manoeuvre bâtiment",
        description = Some("L'agence Osmose emploi recherche pour l'un de ses clients un MANŒUVRE (H/F) sur le secteur de Pau (64)\n\nVotre mission consiste a préparer le terrain, les outils et les matériaux nécessaires à l'exécution de travaux de construction, de réparation ou d'entretien dans le bâtiment, sur les routes ou voiries,selon les règles de sécurité.\n\nVous pouvez être amené a assister le maçon et développer vos compétences en maçonnerie.\n\nCe poste est ouvert a toutes les personnes qui souhaitent s'investir pleinement et devenir autonome et polyvalent sur différentes tâches."),
        libelleLieuTravail = "64 - PAU",
        typeContrat = "CDD",
        libelleTypeContrat = "Contrat à durée déterminée - 30 Jour(s)",
        libelleSalaire = Some("Mensuel de 2000.00 Euros à 3000.00 Euros sur 12 mois"),
        libelleDureeTravail = "39H Horaires normaux",
        libelleExperience = "Débutant accepté",
        metier = Metier(codeROME = CodeROME("H2903"), label = "Conduite d'équipement d'usinage"),
        competences = List("Appliquer les mesures correctives", "Contrôler un produit fini"),
        nomEntreprise = Some("OSMOSE EMPLOI"),
        descriptionEntreprise = Some("Agence d'emplois, travail temporaire, Placements CDD, CDI"),
        effectifEntreprise = None,
        nomContact = Some("Mme Aurelie TOULET"),
        telephoneContact = None,
        emailContact = Some(Email("offre29086632.8@osmose-emploi.contactrh.com")),
        urlPostuler = None,
        coordonneesContact = None,
        dateActualisation = LocalDateTime.now()
      )
    else if (n % 2 == 0)
      Offre(
        id = OffreId(UUID.randomUUID().toString),
        intitule = "Responsable contrôle métrologie en industrie",
        description = Some("Rattaché à notre Business Line Chemicals & Pharma, le Responsable Métrologie H/F a pour principales missions : \n\n- Planifier et assurer la gestion métrologique des équipements ainsi que le suivi des maintenances (mise à jour, réparation)\n- Gérer et optimiser les consommables (envoi/réception et gestion des stocks)\n- Prendre en charge la gestion documentaire associée (gestion, archivage, mise en ligne des certificats)\n- Participer à la sélection et assurer le suivi des fournisseurs\n- Piloter la qualification des équipements selon les procédures en place\n- Participer à la rédaction des documents en rapport avec les équipements\n- Réaliser une veille sur les équipements et les nouvelles technologies\n\nBAC+2 Maintenance des Systèmes, maintenance industrielle/métrologie.\nVous avez une connaissance des équipements de laboratoire d'analyses et/ou formation en métrologie.\nVous maîtrisez Word et Excel. \nDes déplacements ponctuels sont à prévoir./F) sur le secteur de Pau (64)\n\nVotre mission consiste a préparer le terrain, les outils et les matériaux nécessaires à l'exécution de travaux de construction, de réparation ou d'entretien dans le bâtiment, sur les routes ou voiries,selon les règles de sécurité.\n\nVous pouvez être amené a assister le maçon et développer vos compétences en maçonnerie.\n\nCe poste est ouvert a toutes les personnes qui souhaitent s'investir pleinement et devenir autonome et polyvalent sur différentes tâches."),
        libelleLieuTravail = "27 - HEUDEBOUVILLE",
        typeContrat = "CDI",
        libelleTypeContrat = "Contrat à durée indéterminée",
        libelleSalaire = Some("Mensuel de 2000.00 Euros à 3000.00 Euros sur 12 mois"),
        libelleDureeTravail = "39H Horaires normaux",
        libelleExperience = "Débutant accepté",
        metier = Metier(codeROME = CodeROME("H2903"), label = "Conduite d'équipement d'usinage"),
        competences = Nil,
        nomEntreprise = Some("INTERTEK FRANCE"),
        descriptionEntreprise = Some("Entreprise de Charpente Couverture, Maison ossature bois, Escaliéteur, Dal'Alu, composée de 18 salariés. Entreprise créée il y a plus de 35 ans, disposant d'une excellente image dans la région grâce à l'aide de son équipe jeune, passionnée, formée en majorité dans cette entreprise chez les Compagnons du devoir."),
        effectifEntreprise = Some("3 à 5 salariés"),
        nomContact = Some("M. Bernard Baue"),
        telephoneContact = Some(NumeroTelephone("0169010215")),
        emailContact = None,
        urlPostuler = None,
        coordonneesContact = None,
        dateActualisation = LocalDateTime.now()
      )
    else
      Offre(
        id = OffreId(UUID.randomUUID().toString),
        intitule = "Agent de fabrication polyvalent / Agente de fabrication pol (H/F)",
        description = None,
        libelleLieuTravail = "57 - STE MARIE AUX CHENES",
        typeContrat = "MIS",
        libelleTypeContrat = "Travail intérimaire - 3 Mois",
        libelleSalaire = Some("Mensuel de 2000.00 Euros à 3000.00 Euros sur 12 mois"),
        libelleDureeTravail = "39H Horaires normaux",
        libelleExperience = "Débutant accepté",
        metier = Metier(codeROME = CodeROME("H2903"), label = "Conduite d'équipement d'usinage"),
        competences = List("Détecter un dysfonctionnement", "Surveiller le déroulement de l'usinage"),
        nomEntreprise = Some("FAB PRODUITS POUR COLLECTIVITES"),
        descriptionEntreprise = Some("Entreprise de Charpente Couverture, Maison ossature bois, Escaliéteur, Dal'Alu, composée de 18 salariés. Entreprise créée il y a plus de 35 ans, disposant d'une excellente image dans la région grâce à l'aide de son équipe jeune, passionnée, formée en majorité dans cette entreprise chez les Compagnons du devoir."),
        effectifEntreprise = Some("20 à 30 salariés"),
        nomContact = Some("Mme ALEXANDRA SAULNERON"),
        telephoneContact = None,
        emailContact = None,
        urlPostuler = Some("https://parcasterix-recrute.talent-soft.com/offre-de-emploi/emploi-agent-polyvalent-de-restauration-en-contrat-de-professionnalisation_566.aspx"),
        coordonneesContact = None,
        dateActualisation = LocalDateTime.now()
      )
  ))
}
