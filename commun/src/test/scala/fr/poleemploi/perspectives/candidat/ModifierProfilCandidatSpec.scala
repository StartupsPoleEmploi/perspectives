package fr.poleemploi.perspectives.candidat

import java.time.LocalDate

import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.commun.domain.Coordonnees
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}

import scala.concurrent.Future

class ModifierProfilCandidatSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  val candidatBuilder = new CandidatBuilder

  val adresse: Adresse =
    Adresse(
      voie = "3 rue des oursons",
      codePostal = "75020",
      libelleCommune = "Paris",
      libellePays = "France"
    )
  val coordonnees: Coordonnees =
    Coordonnees(
      latitude = 48.8510418,
      longitude = 2.3755859
    )
  val statutDemandeurEmploi: StatutDemandeurEmploi = StatutDemandeurEmploi.DEMANDEUR_EMPLOI

  val langue: Langue = Langue(
    label = "Allemand",
    niveau = Some(NiveauLangue.COURANT)
  )
  val permis: Permis = Permis(
    code = "B",
    label = "Véhicule léger"
  )
  val savoirFaire: SavoirFaire = SavoirFaire(
    label = "Réaliser une opération d'affûtage",
    niveau = Some(NiveauSavoirFaire.AVANCE)
  )
  val formation: Formation = Formation(
    anneeFin = 2019,
    intitule = "MOOC en ligne",
    lieu = Some("Paris"),
    domaine = Some(DomaineFormation("Informatique de gestion")),
    niveau = Some(NiveauFormation("BAC+5 et plus ou équivalent"))
  )
  val experienceProfessionnelle: ExperienceProfessionnelle = ExperienceProfessionnelle(
    dateDebut = LocalDate.now().minusYears(1L),
    dateFin = Some(LocalDate.now()),
    enPoste = true,
    intitule = "Chauffeur poids lourd",
    nomEntreprise = Some("DISTRANS"),
    lieu = Some("Paris"),
    description = None
  )

  val commande: ModifierProfilCandidatCommand =
    ModifierProfilCandidatCommand(
      id = candidatBuilder.candidatId,
      adresse = None,
      statutDemandeurEmploi = None,
      centresInteret = Nil,
      langues = Nil,
      permis = Nil,
      savoirEtre = Nil,
      savoirFaire = Nil,
      formations = Nil,
      experiencesProfessionnelles = Nil
    )

  var localisationService: LocalisationService = _

  before {
    localisationService = mock[LocalisationService]
    when(localisationService.localiser(adresse)) thenReturn Future.successful(Some(coordonnees))
  }

  "modifierProfil" should {
    "ne pas générer d'événement de modification d'adresse lorsque le service de localisation échoue (pas d'intérêt pour la recherche)" in {
      // Given
      val nouvelleAdresse = adresse.copy(voie = "nouvelle voie")
      when(localisationService.localiser(nouvelleAdresse)) thenReturn Future.failed(new RuntimeException("erreur de service"))
      val candidat = candidatBuilder
        .avecInscription()
        .avecAdresse(adresse.copy(voie = "ancienne voie"))
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        adresse = Some(nouvelleAdresse)
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[AdresseModifieeEvent]) mustBe 0)
    }
    "ne pas générer d'événement de modification d'adresse lorsque l'adresse n'a pas de coordonnées (pas d'intérêt pour la recherche)" in {
      // Given
      val nouvelleAdresse = adresse.copy(voie = "nouvelle voie")
      when(localisationService.localiser(nouvelleAdresse)) thenReturn Future.successful(None)
      val candidat = candidatBuilder
        .avecInscription()
        .avecAdresse(adresse.copy(voie = "ancienne voie"))
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        adresse = Some(nouvelleAdresse)
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[AdresseModifieeEvent]) mustBe 0)
    }
    "ne pas générer d'événement de modification d'adresse si elle a été modifiée, mais que ces coordonnées GPS restent les mêmes (modification d'un libellé simple de rue)" in {
      // Given
      val nouvelleAdresse = adresse.copy(voie = "16 rue notre-dame")
      when(localisationService.localiser(nouvelleAdresse)) thenReturn Future.successful(Some(coordonnees))
      val candidat = candidatBuilder
        .avecInscription()
        .avecAdresse(
          adresse = adresse.copy(voie = "16 rue notre dame"),
          coordonnees = Some(coordonnees)
        )
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        adresse = Some(nouvelleAdresse)
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[AdresseModifieeEvent]) mustBe 0)
    }
    "ne pas générer d'événement de modification d'adresse si elle est supprimée (on ne supprime pas d'information)" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecAdresse(adresse)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        adresse = None
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[AdresseModifieeEvent]) mustBe 0)
    }
    "générer un événement lorsque l'adresse a été modifiée" in {
      // Given
      val nouvelleAdresse = adresse.copy(voie = "33 boulevard soultz")
      when(localisationService.localiser(nouvelleAdresse)) thenReturn Future.successful(Some(coordonnees))
      val candidat = candidatBuilder
        .avecInscription()
        .avecAdresse(
          adresse.copy(voie = "52 boulevard soultz"),
          coordonnees = Some(coordonnees.copy(latitude = coordonnees.latitude + 1))
        )
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        adresse = Some(nouvelleAdresse)
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[AdresseModifieeEvent]) mustBe 1)
    }
    "générer un événement contenant l'adresse modifiée" in {
      // Given
      val nouvelleAdresse = adresse.copy(voie = "nouvelle voie")
      val coordonnees = mock[Coordonnees]
      when(localisationService.localiser(nouvelleAdresse)) thenReturn Future.successful(Some(coordonnees))
      val candidat = candidatBuilder
        .avecInscription()
        .avecAdresse(adresse.copy(voie = "ancienne voie"))
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        adresse = Some(nouvelleAdresse)
      ), localisationService)

      // Then
      future map (events => {
        val event = events.filter(_.isInstanceOf[AdresseModifieeEvent]).head.asInstanceOf[AdresseModifieeEvent]
        event.candidatId mustBe commande.id
        event.adresse.voie mustBe "nouvelle voie"
        event.coordonnees mustBe coordonnees
      })
    }
    "ne pas générer d'événement contenant le statut de demandeur d'emploi s'il n'a pas été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecStatutDemandeurEmploi(StatutDemandeurEmploi.DEMANDEUR_EMPLOI)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        statutDemandeurEmploi = Some(StatutDemandeurEmploi.DEMANDEUR_EMPLOI)
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[StatutDemandeurEmploiModifieEvent]) mustBe 0)
    }
    "ne pas générer d'événement contenant le statut de demandeur d'emploi lorsqu'il est supprimé (on ne supprime pas d'information)" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecStatutDemandeurEmploi(StatutDemandeurEmploi.DEMANDEUR_EMPLOI)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        statutDemandeurEmploi = None
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[StatutDemandeurEmploiModifieEvent]) mustBe 0)
    }
    "générer un événement contenant le statut de demandeur d'emploi s'il a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecStatutDemandeurEmploi(StatutDemandeurEmploi.DEMANDEUR_EMPLOI)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        statutDemandeurEmploi = Some(StatutDemandeurEmploi.NON_DEMANDEUR_EMPLOI)
      ), localisationService)

      // Then
      future map (events => {
        val event = events.filter(_.isInstanceOf[StatutDemandeurEmploiModifieEvent]).head.asInstanceOf[StatutDemandeurEmploiModifieEvent]
        event.candidatId mustBe commande.id
        event.statutDemandeurEmploi mustBe StatutDemandeurEmploi.NON_DEMANDEUR_EMPLOI
      })
    }
    "ne pas générer d'événement contenant les centres d'intérêt s'ils ne sont pas renseignés" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        centresInteret = Nil
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[CentresInteretModifiesEvent]) mustBe 0)
    }
    "ne pas générer d'événement contenant les centres d'intérêt s'ils n'ont pas été modifiés" in {
      // Given
      val centresInteret = List(CentreInteret("Pêche"))
      val candidat = candidatBuilder
        .avecInscription()
        .avecCentresInteret(centresInteret)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        centresInteret = centresInteret
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[CentresInteretModifiesEvent]) mustBe 0)
    }
    "ne pas générer d'événement contenant les centres d'intérêt s'ils n'ont pas été modifiés et qu'ils ne sont pas dans le même ordre" in {
      // Given
      val centresInteret = List(CentreInteret("Pêche"), CentreInteret("Chasse"))
      val candidat = candidatBuilder
        .avecInscription()
        .avecCentresInteret(centresInteret)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        centresInteret = centresInteret.sortBy(_.value)
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[CentresInteretModifiesEvent]) mustBe 0)
    }
    "générer un événement contenant les centres d'intérêt si on ajoute un centre d'intérêt" in {
      // Given
      val centresInteret = List(CentreInteret("Pêche"))
      val candidat = candidatBuilder
        .avecInscription()
        .avecCentresInteret(centresInteret)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        centresInteret = CentreInteret("Chasse") :: centresInteret
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[CentresInteretModifiesEvent]) mustBe 1)
    }
    "générer un événement contenant les centres d'intérêt si on supprime un centre d'intérêt" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecCentresInteret(List(CentreInteret("Pêche")))
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        centresInteret = Nil
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[CentresInteretModifiesEvent]) mustBe 1)
    }
    "générer un événement contenant les informations des centres d'intérêt" in {
      // Given
      val centresInteret = List(CentreInteret("Chasse"))
      val candidat = candidatBuilder
        .avecInscription()
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        centresInteret = centresInteret
      ), localisationService)

      // Then
      future map (events => {
        val event = events.filter(_.isInstanceOf[CentresInteretModifiesEvent]).head.asInstanceOf[CentresInteretModifiesEvent]
        event.candidatId mustBe commande.id
        event.centresInteret mustBe centresInteret
      })
    }
    "ne pas générer d'événement contenant les langues si elles ne sont pas renseignées" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        langues = Nil
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[LanguesModifieesEvent]) mustBe 0)
    }
    "ne pas générer d'événement contenant les langues si elles n'ont pas été modifiées" in {
      // Given
      val langues = List(langue)
      val candidat = candidatBuilder
        .avecInscription()
        .avecLangues(langues)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        langues = langues
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[LanguesModifieesEvent]) mustBe 0)
    }
    "ne pas générer d'événement contenant les langues si elles n'ont pas été modifiées et qu'elles ne sont pas dans le même ordre" in {
      // Given
      val langues = List(
        Langue(label = "Français", niveau = None),
        Langue(label = "Allemand", niveau = Some(NiveauLangue.COURANT))
      )
      val candidat = candidatBuilder
        .avecInscription()
        .avecLangues(langues)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        langues = langues.sortBy(_.label)
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[LanguesModifieesEvent]) mustBe 0)
    }
    "générer un événement contenant les langues si on ajoute une langue" in {
      // Given
      val langues = List(langue)
      val candidat = candidatBuilder
        .avecInscription()
        .avecLangues(langues)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        langues = Langue(label = "Russe", niveau = Some(NiveauLangue.DEBUTANT)) :: langues
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[LanguesModifieesEvent]) mustBe 1)
    }
    "générer un événement contenant les langues si on modifie une langue" in {
      // Given
      val langues = List(langue.copy(niveau = Some(NiveauLangue.DEBUTANT)))
      val candidat = candidatBuilder
        .avecInscription()
        .avecLangues(langues)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        langues = List(langue.copy(niveau = Some(NiveauLangue.INTERMEDIAIRE)))
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[LanguesModifieesEvent]) mustBe 1)
    }
    "générer un événement contenant les langues si on supprime une langue" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecLangues(List(langue))
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        langues = Nil
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[LanguesModifieesEvent]) mustBe 1)
    }
    "générer un événement contenant les informations des langues" in {
      // Given
      val langues = List(langue)
      val candidat = candidatBuilder
        .avecInscription()
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        langues = langues
      ), localisationService)

      // Then
      future map (events => {
        val event = events.filter(_.isInstanceOf[LanguesModifieesEvent]).head.asInstanceOf[LanguesModifieesEvent]
        event.candidatId mustBe commande.id
        event.langues mustBe langues
      })
    }
    "ne pas générer d'événement contenant les permis s'ils ne sont pas renseignés" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        permis = Nil
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[PermisModifiesEvent]) mustBe 0)
    }
    "ne pas générer d'événement contenant les permis s'ils n'ont pas été modifiés" in {
      // Given
      val listePermis = List(permis)
      val candidat = candidatBuilder
        .avecInscription()
        .avecPermis(listePermis)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        permis = listePermis
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[PermisModifiesEvent]) mustBe 0)
    }
    "ne pas générer d'événement contenant les permis s'ils n'ont pas été modifiés et qu'ils ne sont pas dans le même ordre" in {
      // Given
      val listePermis = List(
        Permis(code = "B1", label = "Quadricycle"),
        Permis(code = "B", label = "Véhicule léger")
      )
      val candidat = candidatBuilder
        .avecInscription()
        .avecPermis(listePermis.sortBy(_.code))
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        permis = listePermis
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[PermisModifiesEvent]) mustBe 0)
    }
    "générer un événement contenant les permis si on ajoute un permis" in {
      // Given
      val listePermis = List(permis)
      val candidat = candidatBuilder
        .avecInscription()
        .avecPermis(listePermis)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        permis = Permis(code = "C", label = "Poids lourd") :: listePermis
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[PermisModifiesEvent]) mustBe 1)
    }
    "générer un événement contenant les permis si on modifie un permis" in {
      // Given
      val listePermis = List(permis.copy(label = "Véhicule léger"))
      val candidat = candidatBuilder
        .avecInscription()
        .avecPermis(listePermis)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        permis = List(permis.copy(label = "Véhicule semi-léger"))
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[PermisModifiesEvent]) mustBe 1)
    }
    "générer un événement contenant les permis si on supprime un permis" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecPermis(List(permis))
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        permis = Nil
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[PermisModifiesEvent]) mustBe 1)
    }
    "générer un événement contenant les informations des permis" in {
      // Given
      val listePermis = List(permis)
      val candidat = candidatBuilder
        .avecInscription()
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        permis = listePermis
      ), localisationService)

      // Then
      future map (events => {
        val event = events.filter(_.isInstanceOf[PermisModifiesEvent]).head.asInstanceOf[PermisModifiesEvent]
        event.candidatId mustBe commande.id
        event.permis mustBe listePermis
      })
    }
    "ne pas générer d'événement contenant les savoirEtre s'ils ne sont pas renseignés" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        savoirEtre = Nil
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[SavoirEtreModifiesEvent]) mustBe 0)
    }
    "ne pas générer d'événement contenant les savoirEtre s'ils n'ont pas été modifiés" in {
      // Given
      val savoirEtre = List(SavoirEtre("Curiosité"))
      val candidat = candidatBuilder
        .avecInscription()
        .avecSavoirEtre(savoirEtre)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        savoirEtre = savoirEtre
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[SavoirEtreModifiesEvent]) mustBe 0)
    }
    "ne pas générer d'événement contenant les savoirEtre s'ils n'ont pas été modifiés et qu'ils ne sont pas dans le même ordre" in {
      // Given
      val savoirEtre = List(SavoirEtre("Rigueur"), SavoirEtre("Curiosité"))
      val candidat = candidatBuilder
        .avecInscription()
        .avecSavoirEtre(savoirEtre)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        savoirEtre = savoirEtre.sortBy(_.value)
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[SavoirEtreModifiesEvent]) mustBe 0)
    }
    "générer un événement contenant les savoirEtre si on ajoute un savoirEtre" in {
      // Given
      val savoirEtre = List(SavoirEtre("Curiosité"))
      val candidat = candidatBuilder
        .avecInscription()
        .avecSavoirEtre(savoirEtre)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        savoirEtre = SavoirEtre("Travail en équipe") :: savoirEtre
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[SavoirEtreModifiesEvent]) mustBe 1)
    }
    "générer un événement contenant les savoirEtre si on modifie un savoirEtre" in {
      // Given
      val savoirEtre = List(SavoirEtre("Curiosité"))
      val candidat = candidatBuilder
        .avecInscription()
        .avecSavoirEtre(savoirEtre)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        savoirEtre = List(SavoirEtre("Travail en équipe"))
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[SavoirEtreModifiesEvent]) mustBe 1)
    }
    "générer un événement contenant les savoirEtre si on supprime un savoirEtre" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecSavoirEtre(List(SavoirEtre("Curiosité")))
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        savoirEtre = Nil
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[SavoirEtreModifiesEvent]) mustBe 1)
    }
    "générer un événement contenant les informations des savoirEtre" in {
      // Given
      val savoirEtre = List(SavoirEtre("Curiosité"))
      val candidat = candidatBuilder
        .avecInscription()
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        savoirEtre = savoirEtre
      ), localisationService)

      // Then
      future map (events => {
        val event = events.filter(_.isInstanceOf[SavoirEtreModifiesEvent]).head.asInstanceOf[SavoirEtreModifiesEvent]
        event.candidatId mustBe commande.id
        event.savoirEtre mustBe savoirEtre
      })
    }
    "ne pas générer d'événement contenant les savoirFaire s'ils ne sont pas renseignés" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        savoirFaire = Nil
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[SavoirFaireModifiesEvent]) mustBe 0)
    }
    "ne pas générer d'événement contenant les savoirFaire s'ils n'ont pas été modifiés" in {
      // Given
      val listeSavoirFaire = List(savoirFaire)
      val candidat = candidatBuilder
        .avecInscription()
        .avecSavoirFaire(listeSavoirFaire)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        savoirFaire = listeSavoirFaire
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[SavoirFaireModifiesEvent]) mustBe 0)
    }
    "ne pas générer d'événement contenant les savoirFaire s'ils n'ont pas été modifiés et qu'ils ne sont pas dans le même ordre" in {
      // Given
      val listeSavoirFaire = List(
        SavoirFaire(label = "Réaliser une opération d'affûtage", niveau = None),
        SavoirFaire(label = "Encaisser des actes médicaux", niveau = None)
      )
      val candidat = candidatBuilder
        .avecInscription()
        .avecSavoirFaire(listeSavoirFaire)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        savoirFaire = listeSavoirFaire.sortBy(_.label)
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[SavoirFaireModifiesEvent]) mustBe 0)
    }
    "générer un événement contenant les savoirFaire si on ajoute un savoirFaire" in {
      // Given
      val listeSavoirFaire = List(savoirFaire)
      val candidat = candidatBuilder
        .avecInscription()
        .avecSavoirFaire(listeSavoirFaire)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        savoirFaire = SavoirFaire(label = "Découper une viande", niveau = None) :: listeSavoirFaire
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[SavoirFaireModifiesEvent]) mustBe 1)
    }
    "générer un événement contenant les savoirFaire si on modifie un savoirFaire" in {
      // Given
      val listeSavoirFaire = List(savoirFaire.copy(niveau = None))
      val candidat = candidatBuilder
        .avecInscription()
        .avecSavoirFaire(listeSavoirFaire)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        savoirFaire = List(savoirFaire.copy(niveau = Some(NiveauSavoirFaire.AVANCE)))
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[SavoirFaireModifiesEvent]) mustBe 1)
    }
    "générer un événement contenant les savoirFaire si on supprime un savoirFaire" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecSavoirFaire(List(savoirFaire))
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        savoirFaire = Nil
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[SavoirFaireModifiesEvent]) mustBe 1)
    }
    "générer un événement contenant les informations des savoirFaire" in {
      // Given
      val listeSavoirFaire = List(savoirFaire)
      val candidat = candidatBuilder
        .avecInscription()
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        savoirFaire = listeSavoirFaire
      ), localisationService)

      // Then
      future map (events => {
        val event = events.filter(_.isInstanceOf[SavoirFaireModifiesEvent]).head.asInstanceOf[SavoirFaireModifiesEvent]
        event.candidatId mustBe commande.id
        event.savoirFaire mustBe listeSavoirFaire
      })
    }
    "ne pas générer d'événement contenant les formations si elles ne sont pas renseignées" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        formations = Nil
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[FormationsModifieesEvent]) mustBe 0)
    }
    "ne pas générer d'événement contenant les formations si elles n'ont pas été modifiées" in {
      // Given
      val formations = List(formation)
      val candidat = candidatBuilder
        .avecInscription()
        .avecFormations(formations)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        formations = formations
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[FormationsModifieesEvent]) mustBe 0)
    }
    "ne pas générer d'événement contenant les formations si elles n'ont pas été modifiées et qu'elles ne sont pas dans le même ordre" in {
      // Given
      val formations = List(
        formation.copy(anneeFin = 2018, intitule = "Boucher"),
        formation.copy(anneeFin = 2019, intitule = "Agriculteur")
      )
      val candidat = candidatBuilder
        .avecInscription()
        .avecFormations(formations)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        formations = formations.sortBy(_.intitule)
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[FormationsModifieesEvent]) mustBe 0)
    }
    "générer un événement contenant les formations si on ajoute une formation" in {
      // Given
      val nouvelleFormation = formation.copy(intitule = "Nouvelle formation")
      val formations = List(formation)
      val candidat = candidatBuilder
        .avecInscription()
        .avecFormations(formations)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        formations = nouvelleFormation :: formations
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[FormationsModifieesEvent]) mustBe 1)
    }
    "générer un événement contenant les formations si on modifie une formation" in {
      // Given
      val formations = List(formation.copy(anneeFin = 2018))
      val candidat = candidatBuilder
        .avecInscription()
        .avecFormations(formations)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        formations = List(formation.copy(anneeFin = 2019))
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[FormationsModifieesEvent]) mustBe 1)
    }
    "générer un événement contenant les formations si on supprime une formation" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecFormations(List(formation))
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        formations = Nil
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[FormationsModifieesEvent]) mustBe 1)
    }
    "générer un événement contenant les informations des formations" in {
      // Given
      val formations = List(formation)
      val candidat = candidatBuilder
        .avecInscription()
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        formations = formations
      ), localisationService)

      // Then
      future map (events => {
        val event = events.filter(_.isInstanceOf[FormationsModifieesEvent]).head.asInstanceOf[FormationsModifieesEvent]
        event.candidatId mustBe commande.id
        event.formations mustBe formations
      })
    }
    "ne pas générer d'événement contenant les expériences si elles ne sont pas renseignées" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        experiencesProfessionnelles = Nil
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[ExperiencesProfessionnellesModifieesEvent]) mustBe 0)
    }
    "ne pas générer d'événement contenant les expériences si elles n'ont pas été modifiées" in {
      // Given
      val experiencesProfessionnelles = List(experienceProfessionnelle)
      val candidat = candidatBuilder
        .avecInscription()
        .avecExperiencesProfessionnelles(experiencesProfessionnelles)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        experiencesProfessionnelles = experiencesProfessionnelles
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[ExperiencesProfessionnellesModifieesEvent]) mustBe 0)
    }
    "ne pas générer d'événement contenant les expériences si elles n'ont pas été modifiées et qu'elles ne sont pas dans le même ordre" in {
      // Given
      val experiencesProfessionnelles = List(
        experienceProfessionnelle.copy(dateDebut = LocalDate.now(), intitule = "Développeur"),
        experienceProfessionnelle.copy(dateDebut = LocalDate.now().minusYears(1), intitule = "Boucher")
      )
      val candidat = candidatBuilder
        .avecInscription()
        .avecExperiencesProfessionnelles(experiencesProfessionnelles)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        experiencesProfessionnelles = experiencesProfessionnelles.sortBy(_.intitule)
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[ExperiencesProfessionnellesModifieesEvent]) mustBe 0)
    }
    "générer un événement contenant les expériences si on ajoute une expérience" in {
      // Given
      val nouvelleExperience = experienceProfessionnelle.copy(
        dateDebut = LocalDate.now().minusYears(5),
        intitule = "Développeur"
      )
      val experiencesProfessionnelles = List(experienceProfessionnelle)
      val candidat = candidatBuilder
        .avecInscription()
        .avecExperiencesProfessionnelles(experiencesProfessionnelles)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        experiencesProfessionnelles = nouvelleExperience :: experiencesProfessionnelles
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[ExperiencesProfessionnellesModifieesEvent]) mustBe 1)
    }
    "générer un événement contenant les expériences si on modifie une expérience" in {
      // Given
      val experiencesProfessionnelles = List(experienceProfessionnelle.copy(dateFin = None))
      val candidat = candidatBuilder
        .avecInscription()
        .avecExperiencesProfessionnelles(experiencesProfessionnelles)
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        experiencesProfessionnelles = List(experienceProfessionnelle.copy(dateFin = Some(LocalDate.now())))
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[ExperiencesProfessionnellesModifieesEvent]) mustBe 1)
    }
    "générer un événement contenant les expériences si on supprime une expérience" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecExperiencesProfessionnelles(List(experienceProfessionnelle))
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        experiencesProfessionnelles = Nil
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[ExperiencesProfessionnellesModifieesEvent]) mustBe 1)
    }
    "générer un événement contenant les informations des experiences" in {
      // Given
      val experiencesProfessionnelles = List(experienceProfessionnelle)
      val candidat = candidatBuilder
        .avecInscription()
        .build

      // When
      val future = candidat.modifierProfil(commande.copy(
        experiencesProfessionnelles = experiencesProfessionnelles
      ), localisationService)

      // Then
      future map (events => {
        val event = events.filter(_.isInstanceOf[ExperiencesProfessionnellesModifieesEvent]).head.asInstanceOf[ExperiencesProfessionnellesModifieesEvent]
        event.candidatId mustBe commande.id
        event.experiencesProfessionnelles mustBe experiencesProfessionnelles
      })
    }
  }
}
