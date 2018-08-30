package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.perspectives.commun.domain.{Email, Genre}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class ModifierProfilGerantSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val recruteurBuilder = new RecruteurBuilder

  val commande: ModifierProfilGerantCommand = ModifierProfilGerantCommand(
    id = recruteurBuilder.recruteurId,
    nom = "nom",
    prenom = "prenom",
    email = Email("email@domain.com"),
    genre = Genre.HOMME
  )

  "modifierProfilGerant" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val recruteur = recruteurBuilder.build

      // When
      val ex = intercept[RuntimeException] {
        recruteur.modifierProfilGerant(commande)
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
      val result = recruteur.modifierProfilGerant(commande)

      // Then
      result.isEmpty mustBe true
    }
    "générer un événement si un critère a été saisi pour la premiere fois" in {
      // Given
      val recruteur = recruteurBuilder.avecInscription().build

      // When
      val result = recruteur.modifierProfilGerant(commande)

      // Then
      result.size mustBe 1
    }
    "générer un événement si le nom a été modifié" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription(nom = Some("ancien nom"))
        .build

      // When
      val result = recruteur.modifierProfilGerant(commande.copy(
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
      val result = recruteur.modifierProfilGerant(commande.copy(
        prenom = "nouveau prénom"
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si l'email a été modifié" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription(email = Some(Email("ancien-email@domain.fr")))
        .build

      // When
      val result = recruteur.modifierProfilGerant(commande.copy(
        email = Email("nouvel-email@domain.fr")
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
      val result = recruteur.modifierProfilGerant(commande.copy(
        genre = Genre.FEMME
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement contenant les informations modifiés" in {
      // Given
      val recruteur = recruteurBuilder.avecInscription().build

      // When
      val result = recruteur.modifierProfilGerant(commande)

      // Then
      val profilGerantModifieEvent = result.head.asInstanceOf[ProfilGerantModifieEvent]
      profilGerantModifieEvent.recruteurId mustBe commande.id
      profilGerantModifieEvent.nom mustBe commande.nom
      profilGerantModifieEvent.prenom mustBe commande.prenom
      profilGerantModifieEvent.email mustBe commande.email
      profilGerantModifieEvent.genre mustBe commande.genre
    }
  }
}
