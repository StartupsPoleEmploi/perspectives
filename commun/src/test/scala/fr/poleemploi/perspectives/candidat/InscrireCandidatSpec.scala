package fr.poleemploi.perspectives.candidat

import fr.poleemploi.perspectives.commun.domain.{Email, Genre}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}

class InscrireCandidatSpec extends WordSpec with MustMatchers with MockitoSugar {

  val candidatBuilder = new CandidatBuilder

  val commande: InscrireCandidatCommand =
    InscrireCandidatCommand(
      id = candidatBuilder.candidatId,
      nom = "nom",
      prenom = "prenom",
      email = Email("email@domain.com"),
      genre = Genre.HOMME,
      adresse = None,
      statutDemandeurEmploi = None
    )

  "inscrire" should {
    "renvoyer une erreur lorsque le candidat est déjà inscrit" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When
      val ex = intercept[IllegalArgumentException] {
        candidat.inscrire(commande)
      }

      // Then
      ex.getMessage mustBe s"Le candidat ${candidat.id.value} est déjà inscrit"
    }
    "générer un evenement lorsque le candidat n'est pas encore inscrit" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val result = candidat.inscrire(commande)

      // Then
      result.size mustBe 1
    }
    "générer un événement contenant les informations d'inscription" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val result = candidat.inscrire(commande)

      // Then
      val event = result.filter(_.isInstanceOf[CandidatInscritEvent])
      event.size mustBe 1
      val candidatInscritEvent = event.head.asInstanceOf[CandidatInscritEvent]
      candidatInscritEvent.candidatId mustBe commande.id
      candidatInscritEvent.nom mustBe commande.nom
      candidatInscritEvent.prenom mustBe commande.prenom
      candidatInscritEvent.email mustBe commande.email
      candidatInscritEvent.genre mustBe commande.genre
    }
    "générer un événement contenant l'adresse lorsqu'elle est renseignée" in {
      // Given
      val adresse = mock[Adresse]
      val candidat = candidatBuilder.build

      // When
      val result = candidat.inscrire(commande.copy(adresse = Some(adresse)))

      // Then
      val event = result.filter(_.isInstanceOf[AdresseModifieeEvent])
      event.size mustBe 1
      val adresseModifieeEvent = event.head.asInstanceOf[AdresseModifieeEvent]
      adresseModifieeEvent.candidatId mustBe commande.id
      adresseModifieeEvent.adresse mustBe adresse
    }
    "générer un événement contenant le statut de demandeur d'emploi lorsqu'il est renseigné" in {
      // Given
      val statutDemandeurEmploi = mock[StatutDemandeurEmploi]
      val candidat = candidatBuilder.build

      // When
      val result = candidat.inscrire(commande.copy(statutDemandeurEmploi = Some(statutDemandeurEmploi)))

      // Then
      val event = result.filter(_.isInstanceOf[StatutDemandeurEmploiModifieEvent])
      event.size mustBe 1
      val statutDemandeurEmploiModifieEvent = event.head.asInstanceOf[StatutDemandeurEmploiModifieEvent]
      statutDemandeurEmploiModifieEvent.candidatId mustBe commande.id
      statutDemandeurEmploiModifieEvent.statutDemandeurEmploi mustBe statutDemandeurEmploi
    }
  }

}
