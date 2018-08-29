package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.commun.domain.Genre
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}

class InscrireRecruteurSpec extends WordSpec with MustMatchers with MockitoSugar {

  val recruteurBuilder = new RecruteurBuilder

  val inscrireCommande: InscrireRecruteurCommand =
    InscrireRecruteurCommand(
      id = recruteurBuilder.recruteurId,
      nom = "nom",
      prenom = "prenom",
      email = "email@domain.com",
      genre = Genre.HOMME
    )

  "inscrire" should {
    "renvoyer une erreur lorsque le recruteur est déjà inscrit" in {
      // Given
      val recruteur = recruteurBuilder.avecInscription().build

      // When
      val ex = intercept[RuntimeException] {
        recruteur.inscrire(inscrireCommande)
      }

      // Then
      ex.getMessage mustBe s"Le recruteur ${recruteur.id.value} est déjà inscrit"
    }
    "générer un événement lorsque le recruteur n'est pas encore inscrit" in {
      // Given
      val recruteur = recruteurBuilder.build

      // When
      val events: List[Event] = recruteur.inscrire(inscrireCommande)

      // Then
      events.size mustBe 1
    }
    "générer un événement contenant les informations d'inscription" in {
      // Given
      val recruteur = recruteurBuilder.build

      // When
      val results = recruteur.inscrire(inscrireCommande)

      // Then
      val recruteurInscritEvent = results.head.asInstanceOf[RecruteurInscritEvent]
      recruteurInscritEvent.recruteurId mustBe inscrireCommande.id
      recruteurInscritEvent.nom mustBe inscrireCommande.nom
      recruteurInscritEvent.prenom mustBe inscrireCommande.prenom
      recruteurInscritEvent.email mustBe inscrireCommande.email
      recruteurInscritEvent.genre mustBe inscrireCommande.genre
    }
  }

}
