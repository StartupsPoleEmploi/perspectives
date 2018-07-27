package fr.poleemploi.perspectives.domain.recruteur

import java.util.UUID

import fr.poleemploi.perspectives.domain.NumeroTelephone
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}

class ModifierProfilRecruteurSpec extends WordSpec with MustMatchers with MockitoSugar {

  val recruteurId: RecruteurId = RecruteurId(UUID.randomUUID().toString)

  val modifierProfilCommande: ModifierProfilCommand =
    ModifierProfilCommand(
      id = recruteurId,
      raisonSociale = "raison sociale",
      numeroSiret = NumeroSiret("13000548100010"),
      typeRecruteur = TypeRecruteur.AGENCE_INTERIM,
      numeroTelephone = NumeroTelephone("0123678943"),
      contactParCandidats = true
    )

  val profilModifieEvent: ProfilModifieEvent =
    ProfilModifieEvent(
      typeRecruteur = modifierProfilCommande.typeRecruteur,
      raisonSociale = modifierProfilCommande.raisonSociale,
      numeroSiret = modifierProfilCommande.numeroSiret,
      numeroTelephone = modifierProfilCommande.numeroTelephone,
      contactParCandidats = modifierProfilCommande.contactParCandidats
    )

  "modifierProfil" should {
    "renvoyer une erreur lorsque le recruteur n'est pas encore inscrit" in {
      // Given
      val recruteur = new Recruteur(
        id = recruteurId,
        version = 0,
        events = Nil
      )

      // When
      val ex = intercept[RuntimeException] {
        recruteur.modifierProfil(modifierProfilCommande)
      }

      // Then
      ex.getMessage mustBe s"Le recruteur ${recruteur.id.value} n'est pas encore inscrit"
    }
    "ne pas générer d'événement si aucune information de profil n'a été modifiée" in {
      // Given
      val recruteur = new Recruteur(
        id = recruteurId,
        version = 0,
        events = List(mock[RecruteurInscrisEvent], profilModifieEvent)
      )

      // When
      val results = recruteur.modifierProfil(modifierProfilCommande)

      // Then
      results.isEmpty mustBe true
    }
    "générer un événement si une information de profil a été saisie pour la premiere fois" in {
      // Given
      val recruteur = new Recruteur(
        id = recruteurId,
        version = 0,
        events = List(mock[RecruteurInscrisEvent])
      )

      // When
      val results = recruteur.modifierProfil(modifierProfilCommande)

      // Then
      results.size mustBe 1
    }
    "générer un événement si la raison sociale a été modifiée" in {
      // Given
      val recruteur = new Recruteur(
        id = recruteurId,
        version = 0,
        events = List(mock[RecruteurInscrisEvent], profilModifieEvent.copy(
          raisonSociale = "raison sociale"
        ))
      )

      // When
      val results = recruteur.modifierProfil(modifierProfilCommande.copy(
        raisonSociale = "nouvelle raison sociale"
      ))

      // Then
      results.size mustBe 1
    }
    "générer un événement si le numero de siret a été modifié" in {
      // Given
      val recruteur = new Recruteur(
        id = recruteurId,
        version = 0,
        events = List(mock[RecruteurInscrisEvent], profilModifieEvent.copy(
          numeroSiret = NumeroSiret("13000548100010")
        ))
      )

      // When
      val results = recruteur.modifierProfil(modifierProfilCommande.copy(
        numeroSiret = NumeroSiret("00000000000018")
      ))

      // Then
      results.size mustBe 1
    }
    "générer un événement si le type de recruteur a été modifié" in {
      // Given
      val recruteur = new Recruteur(
        id = recruteurId,
        version = 0,
        events = List(mock[RecruteurInscrisEvent], profilModifieEvent.copy(
          typeRecruteur = TypeRecruteur.ENTREPRISE
        ))
      )

      // When
      val results = recruteur.modifierProfil(modifierProfilCommande.copy(
        typeRecruteur = TypeRecruteur.AGENCE_INTERIM
      ))

      // Then
      results.size mustBe 1
    }
    "générer un événement si le numéro de téléphone a été modifié" in {
      // Given
      val recruteur = new Recruteur(
        id = recruteurId,
        version = 0,
        events = List(mock[RecruteurInscrisEvent], profilModifieEvent.copy(
          numeroTelephone = NumeroTelephone("0897563423")
        ))
      )

      // When
      val results = recruteur.modifierProfil(modifierProfilCommande.copy(
        numeroTelephone = NumeroTelephone("0197564567")
      ))

      // Then
      results.size mustBe 1
    }
    "générer un événement si contactParCandidats a été modifié" in {
      // Given
      val recruteur = new Recruteur(
        id = recruteurId,
        version = 0,
        events = List(mock[RecruteurInscrisEvent], profilModifieEvent.copy(
          contactParCandidats = false
        ))
      )

      // When
      val results = recruteur.modifierProfil(modifierProfilCommande.copy(
        contactParCandidats = true
      ))

      // Then
      results.size mustBe 1
    }
    "générer un événement contenant les informations modifiées" in {
      // Given
      val recruteur = new Recruteur(
        id = recruteurId,
        version = 0,
        events = List(mock[RecruteurInscrisEvent])
      )

      // When
      val results = recruteur.modifierProfil(modifierProfilCommande)

      // Then
      val event = results.head.asInstanceOf[ProfilModifieEvent]
      event.raisonSociale mustBe modifierProfilCommande.raisonSociale
      event.numeroSiret mustBe modifierProfilCommande.numeroSiret
      event.numeroTelephone mustBe modifierProfilCommande.numeroTelephone
      event.typeRecruteur mustBe modifierProfilCommande.typeRecruteur
      event.contactParCandidats mustBe modifierProfilCommande.contactParCandidats
    }
  }

}
