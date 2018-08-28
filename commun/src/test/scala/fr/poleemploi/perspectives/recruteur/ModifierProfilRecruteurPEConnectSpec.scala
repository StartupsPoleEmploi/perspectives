package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.perspectives.commun.domain.Genre
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class ModifierProfilRecruteurPEConnectSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val recruteurBuilder = new RecruteurBuilder

  val commande: ModifierProfilPEConnectCommand = ModifierProfilPEConnectCommand(
    id = recruteurBuilder.recruteurId,
    nom = "nom",
    prenom = "prenom",
    email = "email",
    genre = Genre.HOMME
  )

  "modifierProfilPEConnect" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val recruteur = recruteurBuilder.build

      // When
      val ex = intercept[RuntimeException] {
        recruteur.modifierProfilPEConnect(commande)
      }

      // Then
      ex.getMessage mustBe s"Le recruteur ${recruteur.id.value} n'est pas encore inscrit"
    }
    "ne pas générer d'événement si aucun critère n'a été modifié" in {
      // Given
      val recruteur = recruteurBuilder.avecInscription(
        nom = Some(commande.nom),
        prenom = Some(commande.prenom),
        email = Some(commande.email),
        genre = Some(commande.genre)
      ).build

      // When
      val result = recruteur.modifierProfilPEConnect(commande)

      // Then
      result.isEmpty mustBe true
    }
    "générer un événement si un critère a été saisi pour la premiere fois" in {
      // Given
      val recruteur = recruteurBuilder.avecInscription().build

      // When
      val result = recruteur.modifierProfilPEConnect(commande)

      // Then
      result.size mustBe 1
    }
    "générer un événement si le nom a été modifié" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription(nom = Some("ancien nom"))
        .build

      // When
      val result = recruteur.modifierProfilPEConnect(commande.copy(
        nom = "nouveau nom"
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si le prénom a été modifié" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription(prenom = Some("ancien nom"))
        .build

      // When
      val result = recruteur.modifierProfilPEConnect(commande.copy(
        prenom = "nouveau prénom"
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si l'email a été modifié" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription(email = Some("ancien-email@domain.fr"))
        .build

      // When
      val result = recruteur.modifierProfilPEConnect(commande.copy(
        email = "nouvel-email@domain.fr"
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si le genre a été modifié" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription(genre = Some(Genre.HOMME))
        .build

      // When
      val result = recruteur.modifierProfilPEConnect(commande.copy(
        genre = Genre.FEMME
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement contenant les informations modifiés" in {
      // Given
      val recruteur = recruteurBuilder.avecInscription().build

      // When
      val result = recruteur.modifierProfilPEConnect(commande)

      // Then
      val event = result.head.asInstanceOf[ProfilRecruteurModifiePEConnectEvent]
      event.recruteurId mustBe commande.id
      event.nom mustBe commande.nom
      event.prenom mustBe commande.prenom
      event.email mustBe commande.email
      event.genre mustBe commande.genre
    }
  }
}
