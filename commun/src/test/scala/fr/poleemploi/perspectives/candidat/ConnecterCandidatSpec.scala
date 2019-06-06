package fr.poleemploi.perspectives.candidat

import fr.poleemploi.perspectives.commun.domain._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}

class ConnecterCandidatSpec extends WordSpec with MustMatchers with MockitoSugar {

  val candidatBuilder = new CandidatBuilder

  val commande: ConnecterCandidatCommand =
    ConnecterCandidatCommand(
      id = candidatBuilder.candidatId,
      nom = Nom("nouveau nom"),
      prenom = Prenom("nouveau prenom"),
      email = Email("nouveau email"),
      genre = Genre.HOMME
    )

  "modifierProfil" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = candidatBuilder.build

      // When & Then
      val ex = intercept[IllegalStateException](
        candidat.connecter(commande)
      )

      // Then
      ex.getMessage mustBe s"Le candidat ${candidat.id.value} avec le statut NOUVEAU ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
    }
    "générer un événement de connexion lorsqu'aucune information de profil n'est modifiée" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(
          nom = Some(commande.nom),
          prenom = Some(commande.prenom),
          email = Some(commande.email),
          genre = Some(commande.genre)
        )
        .build

      // When
      val events = candidat.connecter(commande)

      // Then
      events.count(_.isInstanceOf[CandidatConnecteEvent]) mustBe 1
    }
    "générer un événement si une information de profil a été saisie pour la premiere fois" in {
      // Given
      val candidat = candidatBuilder.avecInscription()
        .build

      // When
      val events = candidat.connecter(commande)

      // Then
      events.count(_.isInstanceOf[ProfilCandidatModifieEvent]) mustBe 1
    }
    "générer un événement si le nom a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(nom = Some(Nom("ancien nom")))
        .build

      // When
      val events = candidat.connecter(commande.copy(
        nom = Nom("nouveau nom")
      ))

      // Then
      events.count(_.isInstanceOf[ProfilCandidatModifieEvent]) mustBe 1
    }
    "générer un événement si le prénom a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(prenom = Some(Prenom("ancien prénom")))
        .build

      // When
      val events = candidat.connecter(commande.copy(
        prenom = Prenom("nouveau prénom")
      ))

      // Then
      events.count(_.isInstanceOf[ProfilCandidatModifieEvent]) mustBe 1
    }
    "générer un événement si l'email a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(email = Some(Email("ancien-email@domain.fr")))
        .build

      // When
      val events = candidat.connecter(commande.copy(
        email = Email("nouvel-email@domain.fr")
      ))

      // Then
      events.count(_.isInstanceOf[ProfilCandidatModifieEvent]) mustBe 1
    }
    "générer un événement si le genre a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(genre = Some(Genre.HOMME))
        .build

      // When
      val events = candidat.connecter(commande.copy(
        genre = Genre.FEMME
      ))

      // Then
      events.count(_.isInstanceOf[ProfilCandidatModifieEvent]) mustBe 1
    }
    "générer un événement contenant les informations de profil modifiées" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .build

      // When
      val events = candidat.connecter(commande)

      // Then
      val event = events.filter(_.isInstanceOf[ProfilCandidatModifieEvent]).head.asInstanceOf[ProfilCandidatModifieEvent]
      event.candidatId mustBe commande.id
      event.nom mustBe commande.nom
      event.prenom mustBe commande.prenom
      event.email mustBe commande.email
      event.genre mustBe commande.genre
    }
  }
}
