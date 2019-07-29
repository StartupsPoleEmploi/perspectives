package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.perspectives.commun.domain.NumeroTelephone
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}

class ModifierProfilRecruteurSpec extends WordSpec with MustMatchers with MockitoSugar {

  val recruteurBuilder = new RecruteurBuilder

  val commande: ModifierProfilCommand =
    ModifierProfilCommand(
      id = recruteurBuilder.recruteurId,
      raisonSociale = "raison sociale",
      numeroSiret = NumeroSiret("13000548100010"),
      typeRecruteur = TypeRecruteur.AGENCE_INTERIM,
      numeroTelephone = NumeroTelephone("0123678943"),
      contactParCandidats = true,
      adresse = Adresse(
        codePostal = "75020",
        libelleCommune = "Paris",
        libellePays = "France"
      )
    )

  "modifierProfil" should {
    "renvoyer une erreur lorsque le recruteur n'est pas encore inscrit" in {
      // Given
      val recruteur = recruteurBuilder.build

      // When
      val ex = intercept[IllegalStateException] {
        recruteur.modifierProfil(commande)
      }

      // Then
      ex.getMessage mustBe s"Le recruteur ${commande.id.value} avec le statut NOUVEAU ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
    }
    "ne pas générer d'événement lorsqu'aucune information de profil n'a été modifiée" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil(
          typeRecruteur = Some(commande.typeRecruteur),
          raisonSociale = Some(commande.raisonSociale),
          numeroSiret = Some(commande.numeroSiret),
          numeroTelephone = Some(commande.numeroTelephone),
          contactParCandidats = Some(commande.contactParCandidats)
        )
        .build

      // When
      val result = recruteur.modifierProfil(commande)

      // Then
      result.count(_.isInstanceOf[ProfilModifieEvent]) mustBe 0
    }
    "générer un événement lorsqu'une information de profil a été saisie pour la premiere fois" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil()
        .build

      // When
      val result = recruteur.modifierProfil(commande)

      // Then
      result.count(_.isInstanceOf[ProfilModifieEvent]) mustBe 1
    }
    "générer un événement lorsque la raison sociale a été modifiée" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil(raisonSociale = Some("ancienne raison sociale"))
        .build

      // When
      val result = recruteur.modifierProfil(commande.copy(
        raisonSociale = "nouvelle raison sociale"
      ))

      // Then
      result.count(_.isInstanceOf[ProfilModifieEvent]) mustBe 1
    }
    "générer un événement lorsque le numero de siret a été modifié" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil(numeroSiret = Some(NumeroSiret("13000548100010")))
        .build

      // When
      val result = recruteur.modifierProfil(commande.copy(
        numeroSiret = NumeroSiret("00000000000018")
      ))

      // Then
      result.count(_.isInstanceOf[ProfilModifieEvent]) mustBe 1
    }
    "générer un événement lorsque le type de recruteur a été modifié" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil(typeRecruteur = Some(TypeRecruteur.ENTREPRISE))
        .build

      // When
      val result = recruteur.modifierProfil(commande.copy(
        typeRecruteur = TypeRecruteur.AGENCE_INTERIM
      ))

      // Then
      result.count(_.isInstanceOf[ProfilModifieEvent]) mustBe 1
    }
    "générer un événement lorsque le numéro de téléphone a été modifié" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil(numeroTelephone = Some(NumeroTelephone("0897563423")))
        .build

      // When
      val result = recruteur.modifierProfil(commande.copy(
        numeroTelephone = NumeroTelephone("0197564567")
      ))

      // Then
      result.count(_.isInstanceOf[ProfilModifieEvent]) mustBe 1
    }
    "générer un événement lorsque contactParCandidats a été modifié" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil(contactParCandidats = Some(false))
        .build

      // When
      val result = recruteur.modifierProfil(commande.copy(
        contactParCandidats = true
      ))

      // Then
      result.count(_.isInstanceOf[ProfilModifieEvent]) mustBe 1
    }
    "générer un événement contenant les informations de profil modifiées" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil()
        .build

      // When
      val result = recruteur.modifierProfil(commande)

      // Then
      val event = result.filter(_.isInstanceOf[ProfilModifieEvent]).head.asInstanceOf[ProfilModifieEvent]
      event.recruteurId mustBe commande.id
      event.raisonSociale mustBe commande.raisonSociale
      event.numeroSiret mustBe commande.numeroSiret
      event.numeroTelephone mustBe commande.numeroTelephone
      event.typeRecruteur mustBe commande.typeRecruteur
      event.contactParCandidats mustBe commande.contactParCandidats
    }
    "ne pas générer d'événement lorsque l'adresse n'est pas modifiée" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecAdresse(adresse = Some(commande.adresse))
        .build

      // When
      val result = recruteur.modifierProfil(commande)

      // Then
      result.count(_.isInstanceOf[AdresseRecruteurModifieeEvent]) mustBe 0
    }
    "générer un événement lorsque l'adresse est saisie pour la premiere fois" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .build

      // When
      val result = recruteur.modifierProfil(commande)

      // Then
      result.count(_.isInstanceOf[AdresseRecruteurModifieeEvent]) mustBe 1
    }
    "générer un événement lorsque l'adresse est modifiée" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecAdresse(adresse = Some(Adresse(
          codePostal = "75020",
          libelleCommune = "Paris",
          libellePays = "France"
        )))
        .build

      // When
      val result = recruteur.modifierProfil(commande.copy(
        adresse = Adresse(
          codePostal = "75011",
          libelleCommune = "Paris",
          libellePays = "France"
        )
      ))

      // Then
      result.count(_.isInstanceOf[AdresseRecruteurModifieeEvent]) mustBe 1
    }
    "générer un événement contenant l'adresse modifiée" in {
      // Given
      val recruteur = recruteurBuilder
        .avecInscription()
        .build

      // When
      val result = recruteur.modifierProfil(commande)

      // Then
      val event = result.filter(_.isInstanceOf[AdresseRecruteurModifieeEvent]).head.asInstanceOf[AdresseRecruteurModifieeEvent]
      event.recruteurId mustBe commande.id
      event.adresse mustBe commande.adresse
    }
  }
}
