package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, CodeSecteurActivite}
import fr.poleemploi.perspectives.recruteur.alerte.domain.FrequenceAlerte
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}

class CreerAlerteSpec extends WordSpec with MustMatchers with MockitoSugar {

  val recruteurBuilder = new RecruteurBuilder

  val commande: CreerAlerteCommand =
    CreerAlerteCommand(
      id = recruteurBuilder.recruteurId,
      alerteId = recruteurBuilder.genererAlerteId,
      codeSecteurActivite = None,
      codeROME = None,
      codeDepartement = Some(CodeDepartement("85")),
      frequenceAlerte = FrequenceAlerte.QUOTIDIENNE
    )

  "creerAlerte" should {
    "renvoyer une erreur lorsque le recruteur n'est pas encore inscrit" in {
      // Given
      val recruteur = recruteurBuilder.build

      // When
      val ex = intercept[IllegalArgumentException] {
        recruteur.creerAlerte(commande)
      }

      // Then
      ex.getMessage mustBe s"Le recruteur ${commande.id.value} dans l'état Nouveau ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
    }
    "renvoyer une erreur lorsque le recruteur n'a pas complété son profil" in {
      // Given
      val recruteur = recruteurBuilder.avecInscription().build

      // When
      val ex = intercept[IllegalArgumentException] {
        recruteur.creerAlerte(commande)
      }

      // Then
      ex.getMessage mustBe s"Le recruteur ${commande.id.value} dans l'état Inscrit ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
    }
    "renvoyer une erreur lorsque l'alerte ne comporte aucun critère" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil()
        .build

      // When
      val ex = intercept[IllegalArgumentException] {
        recruteur.creerAlerte(commande.copy(
          codeSecteurActivite = None,
          codeROME = None,
          codeDepartement = None
        ))
      }

      // Then
      ex.getMessage mustBe s"Au moins un critère doit être renseigné pour une alerte"
    }
    "renvoyer une erreur lorsque le recruteur a atteint le nombre maximum d'alertes" in {
      // Given
      val builder = recruteurBuilder
        .avecInscription()
        .avecProfil()
      (1 to 10).toList.foldLeft(builder)((b, acc) => b.avecAlerte(
        codeDepartement = Some(CodeDepartement(acc.toString))
      ))
      val recruteur = builder.build

      // When
      val ex = intercept[IllegalArgumentException] {
        recruteur.creerAlerte(commande)
      }

      // Then
      ex.getMessage mustBe s"Le recruteur ${recruteur.id.value} a atteint le nombre maximum d'alertes"
    }
    "renvoyer une erreur lorsqu'une alerte existe déjà avec les mêmes critères" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil()
        .avecAlerte(
          frequenceAlerte = Some(FrequenceAlerte.QUOTIDIENNE),
          codeSecteurActivite = Some(CodeSecteurActivite("H")),
          codeROME = Some(CodeROME("H2909")),
          codeDepartement = Some(CodeDepartement("85"))
        ).build

      // When
      val ex = intercept[IllegalArgumentException] {
        recruteur.creerAlerte(commande.copy(
          codeSecteurActivite = Some(CodeSecteurActivite("H")),
          codeROME = Some(CodeROME("H2909")),
          codeDepartement = Some(CodeDepartement("85"))
        ))
      }

      // Then
      ex.getMessage must startWith(s"Une alerte existe déjà pour le recruteur ${recruteur.id.value}")
    }
    "générer un événement lorsque l'alerte est crée" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil()
        .build

      // When
      val result = recruteur.creerAlerte(commande)

      // Then
      result.count(_.isInstanceOf[AlerteRecruteurCreeEvent]) mustBe 1
    }
    "générer un événement contenant l'alerte crée" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil()
        .build

      // When
      val result = recruteur.creerAlerte(commande)

      // Then
      val alerteRecruteurCreeEvent = result
        .filter(_.getClass == classOf[AlerteRecruteurCreeEvent])
        .head.asInstanceOf[AlerteRecruteurCreeEvent]
      alerteRecruteurCreeEvent.recruteurId mustBe commande.id
      alerteRecruteurCreeEvent.alerteId mustBe commande.alerteId
      alerteRecruteurCreeEvent.frequence mustBe commande.frequenceAlerte
      alerteRecruteurCreeEvent.codeSecteurActivite mustBe commande.codeSecteurActivite
      alerteRecruteurCreeEvent.codeROME mustBe commande.codeROME
      alerteRecruteurCreeEvent.codeDepartement mustBe commande.codeDepartement
    }
  }

}
