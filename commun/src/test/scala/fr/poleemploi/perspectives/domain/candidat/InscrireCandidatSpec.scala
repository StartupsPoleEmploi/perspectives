package fr.poleemploi.perspectives.domain.candidat

import java.util.UUID

import fr.poleemploi.eventsourcing.{AggregateId, Event}
import fr.poleemploi.perspectives.domain.Genre
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatest.mockito.MockitoSugar

class InscrireCandidatSpec extends WordSpec with MustMatchers with MockitoSugar {

  val aggregateId: AggregateId = AggregateId(UUID.randomUUID().toString)

  val inscrireCommande: InscrireCandidatCommand =
    InscrireCandidatCommand(
      id = aggregateId,
      nom = "nom",
      prenom = "prenom",
      email = "email@domain.com",
      genre = Genre.HOMME
    )

  "inscrire" should {
    "renvoyer une erreur lorsque le candidat est déjà inscrit" in {
      // Given
      val candidat = new Candidat(
        id = aggregateId,
        version = 0,
        events = List(mock[CandidatInscrisEvent])
      )

      // When
      val ex = intercept[RuntimeException] {
        candidat.inscrire(inscrireCommande)
      }

      // Then
      ex.getMessage mustBe s"Le candidat ${candidat.id.value} est déjà inscrit"
    }
    "générer un événement lorsque le candidat n'est pas encore inscrit" in {
      // Given
      val candidat = new Candidat(
        id = aggregateId,
        version = 0,
        events = Nil
      )

      // When
      val events: List[Event] = candidat.inscrire(inscrireCommande)

      // Then
      events.size mustBe 1
    }
    "générer un événement contenant les informations d'inscription" in {
      // Given
      val candidat = new Candidat(
        id = aggregateId,
        version = 0,
        events = Nil
      )

      // When
      val results = candidat.inscrire(inscrireCommande)

      // Then
      val event = results.head.asInstanceOf[CandidatInscrisEvent]
      event.nom mustBe inscrireCommande.nom
      event.prenom mustBe inscrireCommande.prenom
      event.email mustBe inscrireCommande.email
      event.genre mustBe Some(inscrireCommande.genre.code)
    }
  }

}
