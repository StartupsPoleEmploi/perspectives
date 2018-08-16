package fr.poleemploi.perspectives.domain.candidat

import fr.poleemploi.perspectives.domain.Genre
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class ModifierProfilCandidatPEConnectSpec extends WordSpec
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

  val commande: ModifierProfilPEConnectCommand =
    ModifierProfilPEConnectCommand(
      id = candidatBuilder.candidatId,
      nom = "nouveau nom",
      prenom = "nouveau prenom",
      email = "nouveau email",
      genre = Genre.HOMME,
      adresse = adresse,
      statutDemandeurEmploi = StatutDemandeurEmploi.DEMANDEUR_EMPLOI
    )

  "modifierProfilPEConnect" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val ex = intercept[RuntimeException] {
        candidat.modifierProfilPEConnect(commande)
      }

      // Then
      ex.getMessage mustBe s"Le candidat ${candidat.id.value} n'est pas encore inscrit"
    }
    "ne pas générer d'événement si aucune information de profil n'a été modifiée" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(
          nom = Some(commande.nom),
          prenom = Some(commande.prenom),
          email = Some(commande.email),
          genre = Some(commande.genre)
        )
        .avecAdresse(commande.adresse)
        .avecStatutDemandeurEmploi(commande.statutDemandeurEmploi)
        .build

      // When
      val result = candidat.modifierProfilPEConnect(commande)

      // Then
      result.isEmpty mustBe true
    }
    "générer un événement si une information de profil a été saisie pour la premiere fois" in {
      // Given
      val candidat = candidatBuilder.avecInscription()
        .avecAdresse(adresse)
        .avecStatutDemandeurEmploi(statutDemandeurEmploi)
        .build

      // When
      val result = candidat.modifierProfilPEConnect(commande)

      // Then
      result.size mustBe 1
    }
    "générer un événement si le nom a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(nom = Some("ancien nom"))
        .avecAdresse(adresse)
        .avecStatutDemandeurEmploi(statutDemandeurEmploi)
        .build

      // When
      val result = candidat.modifierProfilPEConnect(commande.copy(
        nom = "nouveau nom"
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si le prénom a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(prenom = Some("ancien prénom"))
        .avecAdresse(adresse)
        .avecStatutDemandeurEmploi(statutDemandeurEmploi)
        .build

      // When
      val result = candidat.modifierProfilPEConnect(commande.copy(
        nom = "nouveau prénom"
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si l'email a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(email = Some("ancien-email@domain.fr"))
        .avecAdresse(adresse)
        .avecStatutDemandeurEmploi(statutDemandeurEmploi)
        .build

      // When
      val result = candidat.modifierProfilPEConnect(commande.copy(
        nom = "nouvel-email@domain.fr"
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si le genre a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(genre = Some(Genre.HOMME))
        .avecAdresse(adresse)
        .avecStatutDemandeurEmploi(statutDemandeurEmploi)
        .build

      // When
      val result = candidat.modifierProfilPEConnect(commande.copy(
        genre = Genre.FEMME
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement contenant les informations de profil modifiées" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecAdresse(adresse)
        .avecStatutDemandeurEmploi(statutDemandeurEmploi)
        .build

      // When
      val result = candidat.modifierProfilPEConnect(commande)

      // Then
      val event = result.head.asInstanceOf[ProfilCandidatModifiePEConnectEvent]
      event.candidatId mustBe commande.id
      event.nom mustBe commande.nom
      event.prenom mustBe commande.prenom
      event.email mustBe commande.email
      event.genre mustBe commande.genre
    }
    "générer un événement si l'adresse a été modifiée" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(
          nom = Some(commande.nom),
          prenom = Some(commande.prenom),
          email = Some(commande.email),
          genre = Some(commande.genre)
        )
        .avecAdresse(adresse.copy(voie = "ancienne voie"))
        .avecStatutDemandeurEmploi(statutDemandeurEmploi)
        .build

      // When
      val result = candidat.modifierProfilPEConnect(commande.copy(
        adresse = adresse.copy(voie = "nouvelle voie")
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement contenant l'adresse modifiée" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(
          nom = Some(commande.nom),
          prenom = Some(commande.prenom),
          email = Some(commande.email),
          genre = Some(commande.genre)
        )
        .avecAdresse(adresse.copy(voie = "ancienne voie"))
        .avecStatutDemandeurEmploi(statutDemandeurEmploi)
        .build

      // When
      val result = candidat.modifierProfilPEConnect(commande.copy(
        adresse = adresse.copy(voie = "nouvelle voie")
      ))

      // Then
      val event = result.head.asInstanceOf[AdressePEConnectModifieeEvent]
      event.candidatId mustBe commande.id
      event.adresse.voie mustBe "nouvelle voie"
    }
    "générer un événement contenant le statut de demandeur d'emploi modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(
          nom = Some(commande.nom),
          prenom = Some(commande.prenom),
          email = Some(commande.email),
          genre = Some(commande.genre)
        )
        .avecAdresse(adresse)
        .avecStatutDemandeurEmploi(StatutDemandeurEmploi.DEMANDEUR_EMPLOI)
        .build

      // When
      val result = candidat.modifierProfilPEConnect(commande.copy(
        statutDemandeurEmploi = StatutDemandeurEmploi.NON_DEMANDEUR_EMPLOI
      ))

      // Then
      val event = result.head.asInstanceOf[StatutDemandeurEmploiPEConnectModifieEvent]
      event.candidatId mustBe commande.id
      event.statutDemandeurEmploi mustBe StatutDemandeurEmploi.NON_DEMANDEUR_EMPLOI
    }
  }
}
