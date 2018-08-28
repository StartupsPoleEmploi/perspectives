package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.perspectives.commun.domain.NumeroTelephone
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}

class ModifierProfilRecruteurSpec extends WordSpec with MustMatchers with MockitoSugar {

  val recruteurBuilder = new RecruteurBuilder

  val modifierProfilCommande: ModifierProfilCommand =
    ModifierProfilCommand(
      id = recruteurBuilder.recruteurId,
      raisonSociale = "raison sociale",
      numeroSiret = NumeroSiret("13000548100010"),
      typeRecruteur = TypeRecruteur.AGENCE_INTERIM,
      numeroTelephone = NumeroTelephone("0123678943"),
      contactParCandidats = true
    )

  "modifierProfil" should {
    "renvoyer une erreur lorsque le recruteur n'est pas encore inscrit" in {
      // Given
      val recruteur = recruteurBuilder.build

      // When
      val ex = intercept[RuntimeException] {
        recruteur.modifierProfil(modifierProfilCommande)
      }

      // Then
      ex.getMessage mustBe s"Le recruteur ${recruteur.id.value} n'est pas encore inscrit"
    }
    "ne pas générer d'événement si aucune information de profil n'a été modifiée" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil(
          typeRecruteur = Some(modifierProfilCommande.typeRecruteur),
          raisonSociale = Some(modifierProfilCommande.raisonSociale),
          numeroSiret = Some(modifierProfilCommande.numeroSiret),
          numeroTelephone = Some(modifierProfilCommande.numeroTelephone),
          contactParCandidats = Some(modifierProfilCommande.contactParCandidats)
        )
        .build

      // When
      val results = recruteur.modifierProfil(modifierProfilCommande)

      // Then
      results.isEmpty mustBe true
    }
    "générer un événement si une information de profil a été saisie pour la premiere fois" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil()
        .build

      // When
      val results = recruteur.modifierProfil(modifierProfilCommande)

      // Then
      results.size mustBe 1
    }
    "générer un événement si la raison sociale a été modifiée" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil(raisonSociale = Some("ancienne raison sociale"))
        .build

      // When
      val results = recruteur.modifierProfil(modifierProfilCommande.copy(
        raisonSociale = "nouvelle raison sociale"
      ))

      // Then
      results.size mustBe 1
    }
    "générer un événement si le numero de siret a été modifié" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil(numeroSiret = Some(NumeroSiret("13000548100010")))
        .build

      // When
      val results = recruteur.modifierProfil(modifierProfilCommande.copy(
        numeroSiret = NumeroSiret("00000000000018")
      ))

      // Then
      results.size mustBe 1
    }
    "générer un événement si le type de recruteur a été modifié" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil(typeRecruteur = Some(TypeRecruteur.ENTREPRISE))
        .build

      // When
      val results = recruteur.modifierProfil(modifierProfilCommande.copy(
        typeRecruteur = TypeRecruteur.AGENCE_INTERIM
      ))

      // Then
      results.size mustBe 1
    }
    "générer un événement si le numéro de téléphone a été modifié" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil(numeroTelephone = Some(NumeroTelephone("0897563423")))
        .build

      // When
      val results = recruteur.modifierProfil(modifierProfilCommande.copy(
        numeroTelephone = NumeroTelephone("0197564567")
      ))

      // Then
      results.size mustBe 1
    }
    "générer un événement si contactParCandidats a été modifié" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil(contactParCandidats = Some(false))
        .build

      // When
      val results = recruteur.modifierProfil(modifierProfilCommande.copy(
        contactParCandidats = true
      ))

      // Then
      results.size mustBe 1
    }
    "générer un événement contenant les informations modifiées" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil()
        .build

      // When
      val results = recruteur.modifierProfil(modifierProfilCommande)

      // Then
      val event = results.head.asInstanceOf[ProfilModifieEvent]
      event.recruteurId mustBe modifierProfilCommande.id
      event.raisonSociale mustBe modifierProfilCommande.raisonSociale
      event.numeroSiret mustBe modifierProfilCommande.numeroSiret
      event.numeroTelephone mustBe modifierProfilCommande.numeroTelephone
      event.typeRecruteur mustBe modifierProfilCommande.typeRecruteur
      event.contactParCandidats mustBe modifierProfilCommande.contactParCandidats
    }
  }

}
