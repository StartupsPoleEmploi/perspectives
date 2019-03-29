package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.commun.domain.{Email, Genre}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}

class InscrireRecruteurSpec extends WordSpec with MustMatchers with MockitoSugar {

  val recruteurBuilder = new RecruteurBuilder

  val commande: InscrireRecruteurCommand =
    InscrireRecruteurCommand(
      id = recruteurBuilder.recruteurId,
      nom = "nom",
      prenom = "prenom",
      email = Email("email@domain.com"),
      genre = Genre.HOMME
    )

  "inscrire" should {
    "renvoyer une erreur lorsque le recruteur est déjà inscrit" in {
      // Given
      val recruteur = recruteurBuilder.avecInscription().build

      // When
      val ex = intercept[IllegalStateException] {
        recruteur.inscrire(commande)
      }

      // Then
      ex.getMessage mustBe s"Le recruteur ${commande.id.value} dans l'état Inscrit ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
    }
    "générer un événement lorsque le recruteur n'est pas encore inscrit" in {
      // Given
      val recruteur = recruteurBuilder.build

      // When
      val events: List[Event] = recruteur.inscrire(commande)

      // Then
      events.size mustBe 1
    }
    "générer un événement contenant les informations d'inscription" in {
      // Given
      val recruteur = recruteurBuilder.build

      // When
      val results = recruteur.inscrire(commande)

      // Then
      val recruteurInscritEvent = results.head.asInstanceOf[RecruteurInscritEvent]
      recruteurInscritEvent.recruteurId mustBe commande.id
      recruteurInscritEvent.nom mustBe commande.nom
      recruteurInscritEvent.prenom mustBe commande.prenom
      recruteurInscritEvent.email mustBe commande.email
      recruteurInscritEvent.genre mustBe commande.genre
    }
  }

}
