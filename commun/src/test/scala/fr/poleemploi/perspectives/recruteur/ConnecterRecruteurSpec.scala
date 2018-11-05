package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.perspectives.commun.domain.{Email, Genre}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class ConnecterRecruteurSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val recruteurBuilder = new RecruteurBuilder

  val commande: ConnecterRecruteurCommand = ConnecterRecruteurCommand(
    id = recruteurBuilder.recruteurId,
    nom = "nom",
    prenom = "prenom",
    email = Email("email@domain.com"),
    genre = Genre.HOMME
  )

  "connecter" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val recruteur = recruteurBuilder.build

      // When
      val ex = intercept[IllegalArgumentException] {
        recruteur.connecter(commande)
      }

      // Then
      ex.getMessage mustBe s"Le recruteur ${commande.id.value} dans l'état Nouveau ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
    }
    "générer un événement de connexion lorsqu'aucune information de profil n'est modifiée" in {
      // Given
      val recruteur = recruteurBuilder.avecInscription(
        nom = Some(commande.nom),
        prenom = Some(commande.prenom),
        email = Some(commande.email),
        genre = Some(commande.genre)
      ).build

      // When
      val result = recruteur.connecter(commande)

      // Then
      result.count(_.isInstanceOf[RecruteurConnecteEvent]) mustBe 1
    }
    "générer un événement contenant les informations de connexion" in {
      // Given
      val recruteur = recruteurBuilder.avecInscription().build

      // When
      val result = recruteur.connecter(commande)

      // Then
      val recruteurConnecteEvent = result.head.asInstanceOf[RecruteurConnecteEvent]
      recruteurConnecteEvent.recruteurId mustBe commande.id
      recruteurConnecteEvent.date must not be null
    }
    "générer deux événements si un critère a été modifié" in {
      // Given
      val recruteur = recruteurBuilder.avecInscription().build

      // When
      val result = recruteur.connecter(commande)

      // Then
      result.size mustBe 2
    }
    "générer un événement si le nom a été modifié" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription(nom = Some("ancien nom"))
        .build

      // When
      val result = recruteur.connecter(commande.copy(
        nom = "nouveau nom"
      ))

      // Then
      result.count(_.isInstanceOf[ProfilGerantModifieEvent]) mustBe 1
    }
    "générer un événement si le prénom a été modifié" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription(prenom = Some("ancien nom"))
        .build

      // When
      val result = recruteur.connecter(commande.copy(
        prenom = "nouveau prénom"
      ))

      // Then
      result.count(_.isInstanceOf[ProfilGerantModifieEvent]) mustBe 1
    }
    "générer un événement si l'email a été modifié" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription(email = Some(Email("ancien-email@domain.fr")))
        .build

      // When
      val result = recruteur.connecter(commande.copy(
        email = Email("nouvel-email@domain.fr")
      ))

      // Then
      result.count(_.isInstanceOf[ProfilGerantModifieEvent]) mustBe 1
    }
    "générer un événement si le genre a été modifié" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription(genre = Some(Genre.HOMME))
        .build

      // When
      val result = recruteur.connecter(commande.copy(
        genre = Genre.FEMME
      ))

      // Then
      result.count(_.isInstanceOf[ProfilGerantModifieEvent]) mustBe 1
    }
    "générer un événement contenant les informations modifiés" in {
      // Given
      val recruteur = recruteurBuilder.avecInscription().build

      // When
      val result = recruteur.connecter(commande)

      // Then
      val profilGerantModifieEvent = result
        .filter(_.getClass == classOf[ProfilGerantModifieEvent])
        .head.asInstanceOf[ProfilGerantModifieEvent]
      profilGerantModifieEvent.recruteurId mustBe commande.id
      profilGerantModifieEvent.nom mustBe commande.nom
      profilGerantModifieEvent.prenom mustBe commande.prenom
      profilGerantModifieEvent.email mustBe commande.email
      profilGerantModifieEvent.genre mustBe commande.genre
    }
  }
}
