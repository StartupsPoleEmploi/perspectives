package fr.poleemploi.perspectives.domain.recruteur

import java.util.UUID

import fr.poleemploi.perspectives.domain.Genre
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class ModifierProfilRecruteurPEConnectSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val recruteurId: RecruteurId = RecruteurId(UUID.randomUUID().toString)

  val commande: ModifierProfilPEConnectCommand = ModifierProfilPEConnectCommand(
    id = recruteurId,
    nom = "nom",
    prenom = "prenom",
    email = "email",
    genre = Genre.HOMME
  )

  val profilModifieEvent =
    ProfilRecruteurModifiePEConnectEvent(
      nom = commande.nom,
      prenom = commande.prenom,
      email = commande.email,
      genre = commande.genre.code
    )

  var recruteurInscrisEvent: RecruteurInscrisEvent = _

  before {
    recruteurInscrisEvent = mock[RecruteurInscrisEvent]
    when(recruteurInscrisEvent.genre) thenReturn Genre.HOMME.code
  }

  "modifierProfilPEConnect" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val recruteur = new Recruteur(
        id = recruteurId,
        version = 0,
        events = Nil
      )

      // When
      val ex = intercept[RuntimeException] {
        recruteur.modifierProfilPEConnect(commande)
      }

      // Then
      ex.getMessage mustBe s"Le recruteur ${recruteur.id.value} n'est pas encore inscrit"
    }
    "ne pas générer d'événement si aucun critère n'a été modifié" in {
      // Given
      val recruteur = new Recruteur(
        id = recruteurId,
        version = 0,
        events = List(recruteurInscrisEvent, profilModifieEvent)
      )

      // When
      val result = recruteur.modifierProfilPEConnect(commande)

      // Then
      result.isEmpty mustBe true
    }
    "générer un événement si un critère a été saisi pour la premiere fois" in {
      // Given
      val recruteur = new Recruteur(
        id = recruteurId,
        version = 0,
        events = List(recruteurInscrisEvent)
      )

      // When
      val result = recruteur.modifierProfilPEConnect(commande)

      // Then
      result.size mustBe 1
    }
    "générer un événement si le nom a été modifié" in {
      // Given
      val recruteur = new Recruteur(
        id = recruteurId,
        version = 0,
        events = List(recruteurInscrisEvent, profilModifieEvent.copy(
          nom = "ancien nom"
        ))
      )

      // When
      val result = recruteur.modifierProfilPEConnect(commande.copy(
        nom = "nouveau nom"
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si le prénom a été modifié" in {
      // Given
      val recruteur = new Recruteur(
        id = recruteurId,
        version = 0,
        events = List(recruteurInscrisEvent, profilModifieEvent.copy(
          nom = "ancien prénom"
        ))
      )

      // When
      val result = recruteur.modifierProfilPEConnect(commande.copy(
        nom = "nouveau prénom"
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si l'email a été modifié" in {
      // Given
      val recruteur = new Recruteur(
        id = recruteurId,
        version = 0,
        events = List(recruteurInscrisEvent, profilModifieEvent.copy(
          nom = "ancien-email@domain.fr"
        ))
      )

      // When
      val result = recruteur.modifierProfilPEConnect(commande.copy(
        nom = "nouvel-email@domain.fr"
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si le genre a été modifié" in {
      // Given
      val recruteur = new Recruteur(
        id = recruteurId,
        version = 0,
        events = List(recruteurInscrisEvent, profilModifieEvent.copy(
          genre = "Homme"
        ))
      )

      // When
      val result = recruteur.modifierProfilPEConnect(commande.copy(
        genre = Genre.FEMME
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement contenant les informations modifiés" in {
      // Given
      val recruteur = new Recruteur(
        id = recruteurId,
        version = 0,
        events = List(recruteurInscrisEvent)
      )

      // When
      val result = recruteur.modifierProfilPEConnect(commande)

      // Then
      val event = result.head.asInstanceOf[ProfilRecruteurModifiePEConnectEvent]
      event.nom mustBe commande.nom
      event.prenom mustBe commande.prenom
      event.email mustBe commande.email
      event.genre mustBe commande.genre.code
    }
  }
}
