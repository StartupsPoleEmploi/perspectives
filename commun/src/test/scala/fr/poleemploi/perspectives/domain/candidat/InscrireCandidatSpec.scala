package fr.poleemploi.perspectives.domain.candidat

import java.util.UUID

import fr.poleemploi.perspectives.domain.Genre
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class InscrireCandidatSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val candidatId: CandidatId = CandidatId(UUID.randomUUID().toString)

  val commande: InscrireCandidatCommand =
    InscrireCandidatCommand(
      id = candidatId,
      nom = "nom",
      prenom = "prenom",
      email = "email@domain.com",
      genre = Genre.HOMME
    )
  var candidatInscrisEvent: CandidatInscrisEvent = _

  before {
    candidatInscrisEvent = mock[CandidatInscrisEvent]
    when(candidatInscrisEvent.genre) thenReturn Some(Genre.HOMME)
  }

  "inscrire" should {
    "renvoyer une erreur lorsque le candidat est déjà inscrit" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent)
      )

      // When
      val ex = intercept[RuntimeException] {
        candidat.inscrire(commande)
      }

      // Then
      ex.getMessage mustBe s"Le candidat ${candidat.id.value} est déjà inscrit"
    }
    "générer un événement lorsque le candidat n'est pas encore inscrit" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = Nil
      )

      // When
      val result = candidat.inscrire(commande)

      // Then
      result.size mustBe 1
    }
    "générer un événement contenant les informations d'inscription" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = Nil
      )

      // When
      val result = candidat.inscrire(commande)

      // Then
      val event = result.head.asInstanceOf[CandidatInscrisEvent]
      event.nom mustBe commande.nom
      event.prenom mustBe commande.prenom
      event.email mustBe commande.email
      event.genre mustBe Some(commande.genre)
    }
  }

}
