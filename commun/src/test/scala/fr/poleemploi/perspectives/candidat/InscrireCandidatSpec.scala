package fr.poleemploi.perspectives.candidat

import fr.poleemploi.perspectives.commun.domain._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class InscrireCandidatSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val candidatBuilder = new CandidatBuilder

  val commande: InscrireCandidatCommand =
    InscrireCandidatCommand(
      id = candidatBuilder.candidatId,
      nom = Nom("nom"),
      prenom = Prenom("prenom"),
      email = Email("email@domain.com"),
      genre = Genre.HOMME
    )

  "inscrire" should {
    "renvoyer une erreur lorsque le candidat est déjà inscrit" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When & Then
      val ex = intercept[IllegalStateException](
        candidat.inscrire(commande)
      )

      // Then
      ex.getMessage mustBe s"Le candidat ${candidat.id.value} avec le statut INSCRIT ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
    }
    "générer un evenement lorsque le candidat n'est pas encore inscrit" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val events = candidat.inscrire(commande)

      // Then
      events.size mustBe 1
    }
    "générer un événement contenant les informations d'inscription" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val events = candidat.inscrire(commande)

      // Then
      val event = events.filter(_.isInstanceOf[CandidatInscritEvent]).head.asInstanceOf[CandidatInscritEvent]
      event.candidatId mustBe commande.id
      event.nom mustBe commande.nom
      event.prenom mustBe commande.prenom
      event.email mustBe commande.email
      event.genre mustBe commande.genre
    }
  }

}
