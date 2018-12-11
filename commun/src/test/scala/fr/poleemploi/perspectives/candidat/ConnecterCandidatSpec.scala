package fr.poleemploi.perspectives.candidat

import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.commun.domain.{Coordonnees, Email, Genre}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}

import scala.concurrent.Future

class ConnecterCandidatSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val candidatBuilder = new CandidatBuilder

  val adresse: Adresse =
    Adresse(
      voie = "3 rue des oursons",
      codePostal = "75020",
      libelleCommune = "Paris",
      libellePays = "France"
    )
  val statutDemandeurEmploi: StatutDemandeurEmploi = StatutDemandeurEmploi.DEMANDEUR_EMPLOI

  val commande: ConnecterCandidatCommand =
    ConnecterCandidatCommand(
      id = candidatBuilder.candidatId,
      nom = "nouveau nom",
      prenom = "nouveau prenom",
      email = Email("nouveau email"),
      genre = Genre.HOMME,
      adresse = None,
      statutDemandeurEmploi = None
    )

  var localisationService: LocalisationService = _

  before {
    localisationService = mock[LocalisationService]
    when(localisationService.localiser(adresse)) thenReturn Future.successful(None)
  }

  "modifierProfil" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = candidatBuilder.build

      // When & Then
      recoverToExceptionIf[IllegalArgumentException] {
        candidat.connecter(commande, localisationService)
      }.map(ex =>
        ex.getMessage mustBe s"Le candidat ${candidat.id.value} dans l'état Nouveau ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
      )
    }
    "générer un événement de connexion lorsqu'aucune information de profil n'est modifiée" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(
          nom = Some(commande.nom),
          prenom = Some(commande.prenom),
          email = Some(commande.email),
          genre = Some(commande.genre)
        )
        .build

      // When
      val future = candidat.connecter(commande, localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[CandidatConnecteEvent]) mustBe 1)
    }
    "générer un événement si une information de profil a été saisie pour la premiere fois" in {
      // Given
      val candidat = candidatBuilder.avecInscription()
        .build

      // When
      val future = candidat.connecter(commande, localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[ProfilCandidatModifieEvent]) mustBe 1)
    }
    "générer un événement si le nom a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(nom = Some("ancien nom"))
        .build

      // When
      val future = candidat.connecter(commande.copy(
        nom = "nouveau nom"
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[ProfilCandidatModifieEvent]) mustBe 1)
    }
    "générer un événement si le prénom a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(prenom = Some("ancien prénom"))
        .build

      // When
      val future = candidat.connecter(commande.copy(
        prenom = "nouveau prénom"
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[ProfilCandidatModifieEvent]) mustBe 1)
    }
    "générer un événement si l'email a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(email = Some(Email("ancien-email@domain.fr")))
        .build

      // When
      val future = candidat.connecter(commande.copy(
        email = Email("nouvel-email@domain.fr")
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[ProfilCandidatModifieEvent]) mustBe 1)
    }
    "générer un événement si le genre a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(genre = Some(Genre.HOMME))
        .build

      // When
      val future = candidat.connecter(commande.copy(
        genre = Genre.FEMME
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[ProfilCandidatModifieEvent]) mustBe 1)
    }
    "générer un événement contenant les informations de profil modifiées" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .build

      // When
      val future = candidat.connecter(commande, localisationService)

      // Then
      future map (events => {
        val event = events.filter(_.isInstanceOf[ProfilCandidatModifieEvent]).head.asInstanceOf[ProfilCandidatModifieEvent]
        event.candidatId mustBe commande.id
        event.nom mustBe commande.nom
        event.prenom mustBe commande.prenom
        event.email mustBe commande.email
        event.genre mustBe commande.genre
      })
    }
    "générer un événement si l'adresse a été modifiée" in {
      // Given
      val nouvelleAdresse = adresse.copy(voie = "nouvelle voie")
      when(localisationService.localiser(nouvelleAdresse)) thenReturn Future.successful(None)
      val candidat = candidatBuilder
        .avecInscription()
        .avecAdresse(adresse.copy(voie = "ancienne voie"))
        .build

      // When
      val future = candidat.connecter(commande.copy(
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
      val future = candidat.connecter(commande.copy(
        adresse = Some(nouvelleAdresse)
      ), localisationService)

      // Then
      future map (events => {
        val event = events.filter(_.isInstanceOf[AdresseModifieeEvent]).head.asInstanceOf[AdresseModifieeEvent]
        event.candidatId mustBe commande.id
        event.adresse.voie mustBe "nouvelle voie"
        event.coordonnees mustBe Some(coordonnees)
      })
    }
    "générer un événement contenant l'adresse modifiée même lorsque le service de localisation échoue" in {
      // Given
      val nouvelleAdresse = adresse.copy(voie = "nouvelle voie")
      when(localisationService.localiser(nouvelleAdresse)) thenReturn Future.failed(new RuntimeException("erreur de service"))
      val candidat = candidatBuilder
        .avecInscription()
        .avecAdresse(adresse.copy(voie = "ancienne voie"))
        .build

      // When
      val future = candidat.connecter(commande.copy(
        adresse = Some(nouvelleAdresse)
      ), localisationService)

      // Then
      future map (events => {
        val event = events.filter(_.isInstanceOf[AdresseModifieeEvent]).head.asInstanceOf[AdresseModifieeEvent]
        event.candidatId mustBe commande.id
        event.adresse.voie mustBe "nouvelle voie"
        event.coordonnees mustBe None
      })
    }
    "générer un événement contenant le statut de demandeur d'emploi modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecStatutDemandeurEmploi(StatutDemandeurEmploi.DEMANDEUR_EMPLOI)
        .build

      // When
      val future = candidat.connecter(commande.copy(
        statutDemandeurEmploi = Some(StatutDemandeurEmploi.NON_DEMANDEUR_EMPLOI)
      ), localisationService)

      // Then
      future map (events => {
        val event = events.filter(_.isInstanceOf[StatutDemandeurEmploiModifieEvent]).head.asInstanceOf[StatutDemandeurEmploiModifieEvent]
        event.candidatId mustBe commande.id
        event.statutDemandeurEmploi mustBe StatutDemandeurEmploi.NON_DEMANDEUR_EMPLOI
      })
    }
  }
}
