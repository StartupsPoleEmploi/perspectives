package fr.poleemploi.perspectives.domain.candidat

import java.util.UUID

import fr.poleemploi.perspectives.domain.Genre
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class ModifierProfilCandidatPEConnectSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val candidatId: CandidatId = CandidatId(UUID.randomUUID().toString)

  val commande: ModifierProfilPEConnectCommand = ModifierProfilPEConnectCommand(
    id = candidatId,
    nom = "nom",
    prenom = "prenom",
    email = "email",
    genre = Genre.HOMME
  )

  val profilModifieEvent =
    ProfilCandidatModifiePEConnectEvent(
      nom = commande.nom,
      prenom = commande.prenom,
      email = commande.email,
      genre = commande.genre
    )

  var candidatInscrisEvent: CandidatInscrisEvent = _

  before {
    candidatInscrisEvent = mock[CandidatInscrisEvent]
    when(candidatInscrisEvent.genre) thenReturn Some(Genre.HOMME)
  }

  "modifierProfilPEConnect" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = Nil
      )

      // When
      val ex = intercept[RuntimeException] {
        candidat.modifierProfilPEConnect(commande)
      }

      // Then
      ex.getMessage mustBe s"Le candidat ${candidat.id.value} n'est pas encore inscrit"
    }
    "ne pas générer d'événement si aucun critère n'a été modifié" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, profilModifieEvent)
      )

      // When
      val result = candidat.modifierProfilPEConnect(commande)

      // Then
      result.isEmpty mustBe true
    }
    "générer un événement si un critère a été saisi pour la premiere fois" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent)
      )

      // When
      val result = candidat.modifierProfilPEConnect(commande)

      // Then
      result.size mustBe 1
    }
    "générer un événement si le nom a été modifié" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, profilModifieEvent.copy(
          nom = "ancien nom"
        ))
      )

      // When
      val result = candidat.modifierProfilPEConnect(commande.copy(
        nom = "nouveau nom"
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si le prénom a été modifié" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, profilModifieEvent.copy(
          nom = "ancien prénom"
        ))
      )

      // When
      val result = candidat.modifierProfilPEConnect(commande.copy(
        nom = "nouveau prénom"
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si l'email a été modifié" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, profilModifieEvent.copy(
          nom = "ancien-email@domain.fr"
        ))
      )

      // When
      val result = candidat.modifierProfilPEConnect(commande.copy(
        nom = "nouvel-email@domain.fr"
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si le genre a été modifié" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, profilModifieEvent.copy(
          genre = Genre.HOMME
        ))
      )

      // When
      val result = candidat.modifierProfilPEConnect(commande.copy(
        genre = Genre.FEMME
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement contenant les informations modifiés" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent)
      )

      // When
      val result = candidat.modifierProfilPEConnect(commande)

      // Then
      val event = result.head.asInstanceOf[ProfilCandidatModifiePEConnectEvent]
      event.nom mustBe commande.nom
      event.prenom mustBe commande.prenom
      event.email mustBe commande.email
      event.genre mustBe commande.genre
    }
  }
}
