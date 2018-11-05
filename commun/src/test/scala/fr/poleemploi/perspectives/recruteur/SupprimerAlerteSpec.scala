package fr.poleemploi.perspectives.recruteur

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}

class SupprimerAlerteSpec extends WordSpec with MustMatchers with MockitoSugar {

  val recruteurBuilder = new RecruteurBuilder

  val commande: SupprimerAlerteCommand =
    SupprimerAlerteCommand(
      id = recruteurBuilder.recruteurId,
      alerteId = recruteurBuilder.genererAlerteId
    )

  "supprimerAlerte" should {
    "renvoyer une erreur lorsque le recruteur n'est pas encore inscrit" in {
      // Given
      val recruteur = recruteurBuilder.build

      // When
      val ex = intercept[IllegalArgumentException] {
        recruteur.supprimerAlerte(commande)
      }

      // Then
      ex.getMessage mustBe s"Le recruteur ${commande.id.value} dans l'état Nouveau ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
    }
    "renvoyer une erreur lorsque le recruteur n'a pas complété son profil" in {
      // Given
      val recruteur = recruteurBuilder.avecInscription().build

      // When
      val ex = intercept[IllegalArgumentException] {
        recruteur.supprimerAlerte(commande)
      }

      // Then
      ex.getMessage mustBe s"Le recruteur ${commande.id.value} dans l'état Inscrit ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
    }
    "renvoyer une erreur lorsque l'alerte n'existe pas" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil()
        .build

      // When
      val ex = intercept[IllegalArgumentException] {
        recruteur.supprimerAlerte(commande)
      }

      // Then
      ex.getMessage mustBe s"L'alerte ${commande.alerteId.value} n'existe pas"
    }
    "générer un événement lorsque l'alerte est supprimée" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil()
        .avecAlerte(alerteId = Some(commande.alerteId))
        .build

      // When
      val result = recruteur.supprimerAlerte(commande)

      // Then
      result.count(_.isInstanceOf[AlerteRecruteurSupprimeeEvent]) mustBe 1
    }
    "générer un événement contenant l'alerte supprimée" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil()
        .avecAlerte(alerteId = Some(commande.alerteId))
        .build

      // When
      val result = recruteur.supprimerAlerte(commande)

      // Then
      val alerteRecruteurSupprimeeEvent = result
        .filter(_.getClass == classOf[AlerteRecruteurSupprimeeEvent])
        .head.asInstanceOf[AlerteRecruteurSupprimeeEvent]
      alerteRecruteurSupprimeeEvent.recruteurId mustBe commande.id
      alerteRecruteurSupprimeeEvent.alerteId mustBe commande.alerteId
    }
  }

}
