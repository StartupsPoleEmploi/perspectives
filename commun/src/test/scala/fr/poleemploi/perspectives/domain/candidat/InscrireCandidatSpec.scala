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
      genre = Genre.HOMME,
      adresse = mock[Adresse]
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
    "générer des événements lorsque le candidat n'est pas encore inscrit" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = Nil
      )

      // When
      val result = candidat.inscrire(commande)

      // Then
      result.size mustBe 2
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
      val event = result.filter(_.isInstanceOf[CandidatInscrisEvent])
      event.size mustBe 1
      val candidatInscrisEvent = event.head.asInstanceOf[CandidatInscrisEvent]
      candidatInscrisEvent.nom mustBe commande.nom
      candidatInscrisEvent.prenom mustBe commande.prenom
      candidatInscrisEvent.email mustBe commande.email
      candidatInscrisEvent.genre mustBe Some(commande.genre)
    }
    "générer un événement contenant l'adresse" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = Nil
      )

      // When
      val result = candidat.inscrire(commande)

      // Then
      val event = result.filter(_.isInstanceOf[AdressePEConnectModifieeEvent])
      event.size mustBe 1
      val adressePEConnectModifieeEvent = event.head.asInstanceOf[AdressePEConnectModifieeEvent]
      adressePEConnectModifieeEvent.adresse mustBe commande.adresse
    }
  }

}
