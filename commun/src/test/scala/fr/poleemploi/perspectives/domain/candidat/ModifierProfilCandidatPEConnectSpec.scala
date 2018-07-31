package fr.poleemploi.perspectives.domain.candidat

import java.util.UUID

import fr.poleemploi.perspectives.domain.Genre
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class ModifierProfilCandidatPEConnectSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val candidatId: CandidatId = CandidatId(UUID.randomUUID().toString)

  val adresse: Adresse =
    Adresse(
      voie = "3 rue des oursons",
      codePostal = "75020",
      libelleCommune = "Paris",
      libellePays = "France"
    )

  val commande: ModifierProfilPEConnectCommand =
    ModifierProfilPEConnectCommand(
      id = candidatId,
      nom = "nom",
      prenom = "prenom",
      email = "email",
      genre = Genre.HOMME,
      adresse = adresse,
      statutDemandeurEmploi = StatutDemandeurEmploi.DEMANDEUR_EMPLOI
    )

  val profilModifieEvent: ProfilCandidatModifiePEConnectEvent =
    ProfilCandidatModifiePEConnectEvent(
      nom = commande.nom,
      prenom = commande.prenom,
      email = commande.email,
      genre = commande.genre
    )
  val adressePEConnectModifieeEvent: AdressePEConnectModifieeEvent =
    AdressePEConnectModifieeEvent(
      adresse = adresse
    )
  val statutDemandeurEmploiPEConnectModifieEvent: StatutDemandeurEmploiPEConnectModifieEvent =
    StatutDemandeurEmploiPEConnectModifieEvent(
      statutDemandeurEmploi = StatutDemandeurEmploi.DEMANDEUR_EMPLOI
    )

  var candidatInscrisEvent: CandidatInscrisEvent = _

  before {
    candidatInscrisEvent = mock[CandidatInscrisEvent]
    when(candidatInscrisEvent.genre) thenReturn Some(Genre.HOMME)
  }

  "modifierProfilPEConnect" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = Nil
      )

      // When
      val ex = intercept[RuntimeException] {
        candidat.modifierProfilPEConnect(commande)
      }

      // Then
      ex.getMessage mustBe s"Le candidat ${candidat.id.value} n'est pas encore inscrit"
    }
    "ne pas générer d'événement si aucune information de profil n'a été modifié" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, profilModifieEvent, adressePEConnectModifieeEvent, statutDemandeurEmploiPEConnectModifieEvent)
      )

      // When
      val result = candidat.modifierProfilPEConnect(commande)

      // Then
      result.isEmpty mustBe true
    }
    "générer un événement si une information de profil a été saisie pour la premiere fois" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, adressePEConnectModifieeEvent, statutDemandeurEmploiPEConnectModifieEvent)
      )

      // When
      val result = candidat.modifierProfilPEConnect(commande)

      // Then
      result.size mustBe 1
    }
    "générer un événement si le nom a été modifié" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, profilModifieEvent.copy(
          nom = "ancien nom"
        ), adressePEConnectModifieeEvent, statutDemandeurEmploiPEConnectModifieEvent)
      )

      // When
      val result = candidat.modifierProfilPEConnect(commande.copy(
        nom = "nouveau nom"
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si le prénom a été modifié" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, profilModifieEvent.copy(
          nom = "ancien prénom"
        ), adressePEConnectModifieeEvent, statutDemandeurEmploiPEConnectModifieEvent)
      )

      // When
      val result = candidat.modifierProfilPEConnect(commande.copy(
        nom = "nouveau prénom"
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si l'email a été modifié" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, profilModifieEvent.copy(
          nom = "ancien-email@domain.fr"
        ), adressePEConnectModifieeEvent, statutDemandeurEmploiPEConnectModifieEvent)
      )

      // When
      val result = candidat.modifierProfilPEConnect(commande.copy(
        nom = "nouvel-email@domain.fr"
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si le genre a été modifié" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, profilModifieEvent.copy(
          genre = Genre.HOMME
        ), adressePEConnectModifieeEvent, statutDemandeurEmploiPEConnectModifieEvent)
      )

      // When
      val result = candidat.modifierProfilPEConnect(commande.copy(
        genre = Genre.FEMME
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement contenant les informations de profil modifiées" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, adressePEConnectModifieeEvent, statutDemandeurEmploiPEConnectModifieEvent)
      )

      // When
      val result = candidat.modifierProfilPEConnect(commande)

      // Then
      val event = result.head.asInstanceOf[ProfilCandidatModifiePEConnectEvent]
      event.nom mustBe commande.nom
      event.prenom mustBe commande.prenom
      event.email mustBe commande.email
      event.genre mustBe commande.genre
    }
    "générer un événement si l'adresse a été modifiée" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events =
          List(candidatInscrisEvent, profilModifieEvent, adressePEConnectModifieeEvent.copy(
          adresse = adresse.copy(voie = "ancienne voie")
        ), statutDemandeurEmploiPEConnectModifieEvent)
      )

      // When
      val result = candidat.modifierProfilPEConnect(commande.copy(
        adresse = adresse
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement contenant l'adresse modifiée" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, profilModifieEvent, statutDemandeurEmploiPEConnectModifieEvent)
      )

      // When
      val result = candidat.modifierProfilPEConnect(commande)

      // Then
      val event = result.head.asInstanceOf[AdressePEConnectModifieeEvent]
      event.adresse mustBe commande.adresse
    }
    "générer un événement contenant le statut de demandeur d'emploi modifié" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, profilModifieEvent, adressePEConnectModifieeEvent)
      )

      // When
      val result = candidat.modifierProfilPEConnect(commande)

      // Then
      val event = result.head.asInstanceOf[StatutDemandeurEmploiPEConnectModifieEvent]
      event.statutDemandeurEmploi mustBe commande.statutDemandeurEmploi
    }
  }
}
