package fr.poleemploi.perspectives.domain.recruteur

import java.util.UUID

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.domain.Genre
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}

class InscrireRecruteurSpec extends WordSpec with MustMatchers with MockitoSugar {

  val recruteurId: RecruteurId = RecruteurId(UUID.randomUUID().toString)

  val inscrireCommande: InscrireRecruteurCommand =
    InscrireRecruteurCommand(
      id = recruteurId,
      nom = "nom",
      prenom = "prenom",
      email = "email@domain.com",
      genre = Genre.HOMME
    )

  "inscrire" should {
    "renvoyer une erreur lorsque le recruteur est déjà inscrit" in {
      // Given
      val recruteur = new Recruteur(
        id = recruteurId,
        version = 0,
        events = List(mock[RecruteurInscrisEvent])
      )

      // When
      val ex = intercept[RuntimeException] {
        recruteur.inscrire(inscrireCommande)
      }

      // Then
      ex.getMessage mustBe s"Le recruteur ${recruteur.id.value} est déjà inscrit"
    }
    "générer un événement lorsque le recruteur n'est pas encore inscrit" in {
      // Given
      val recruteur = new Recruteur(
        id = recruteurId,
        version = 0,
        events = Nil
      )

      // When
      val events: List[Event] = recruteur.inscrire(inscrireCommande)

      // Then
      events.size mustBe 1
    }
    "générer un événement contenant les informations d'inscription" in {
      // Given
      val recruteur = new Recruteur(
        id = recruteurId,
        version = 0,
        events = Nil
      )

      // When
      val results = recruteur.inscrire(inscrireCommande)

      // Then
      val event = results.head.asInstanceOf[RecruteurInscrisEvent]
      event.recruteurId mustBe inscrireCommande.id
      event.nom mustBe inscrireCommande.nom
      event.prenom mustBe inscrireCommande.prenom
      event.email mustBe inscrireCommande.email
      event.genre mustBe inscrireCommande.genre
    }
  }

}
