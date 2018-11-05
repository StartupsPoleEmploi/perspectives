package fr.poleemploi.perspectives.candidat

import fr.poleemploi.perspectives.commun.domain.{CodeROME, NumeroTelephone, RayonRecherche}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class ModifierCriteresCandidatSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val candidatBuilder = new CandidatBuilder

  val commande: ModifierCriteresRechercheCommand =
    ModifierCriteresRechercheCommand(
      id = candidatBuilder.candidatId,
      rechercheAutreMetier = true,
      rechercheMetierEvalue = false,
      metiersRecherches = Set.empty,
      etreContacteParAgenceInterim = true,
      etreContacteParOrganismeFormation = true,
      rayonRecherche = RayonRecherche.MAX_10,
      numeroTelephone = NumeroTelephone("0234567890")
    )

  "modifierCriteres" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val ex = intercept[IllegalArgumentException] {
        candidat.modifierCriteres(commande)
      }

      // Then
      ex.getMessage mustBe s"Le candidat ${candidat.id.value} dans l'état Nouveau ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
    }
    "ne pas générer d'événement si aucun critère n'a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecCriteresRecherche(
          rechercheMetierEvalue = Some(commande.rechercheMetierEvalue),
          rechercheAutreMetier = Some(commande.rechercheAutreMetier),
          metiersRecherches = Some(commande.metiersRecherches),
          etreContacteParAgenceInterim = Some(commande.etreContacteParAgenceInterim),
          etreContacteParOrganismeFormation = Some(commande.etreContacteParOrganismeFormation),
          rayonRecherche = Some(commande.rayonRecherche)
        )
        .avecNumeroTelephone(numeroTelephone = Some(commande.numeroTelephone))
        .build

      // When
      val result = candidat.modifierCriteres(commande)

      // Then
      result.isEmpty mustBe true
    }
    "générer un événement si un critère a été saisi pour la premiere fois" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecCriteresRecherche()
        .avecNumeroTelephone(numeroTelephone = Some(commande.numeroTelephone))
        .build

      // When
      val result = candidat.modifierCriteres(commande)

      // Then
      result.size mustBe 1
    }
    "générer un événement lorsque le numéro de téléphone est saisit la premiere fois" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecCriteresRecherche(
          rechercheMetierEvalue = Some(commande.rechercheMetierEvalue),
          rechercheAutreMetier = Some(commande.rechercheAutreMetier),
          metiersRecherches = Some(commande.metiersRecherches),
          etreContacteParAgenceInterim = Some(commande.etreContacteParAgenceInterim),
          etreContacteParOrganismeFormation = Some(commande.etreContacteParOrganismeFormation),
          rayonRecherche = Some(commande.rayonRecherche)
        )
        .build

      // When
      val result = candidat.modifierCriteres(commande)

      // Then
      result.size mustBe 1
    }
    "générer un événement si rechercheMetierEvalue a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecCriteresRecherche(
          rechercheMetierEvalue = Some(false)
        )
        .avecNumeroTelephone(numeroTelephone = Some(commande.numeroTelephone))
        .build

      // When
      val result = candidat.modifierCriteres(commande.copy(
        rechercheMetierEvalue = true
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si rechercheAutreMetier a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecCriteresRecherche(
          rechercheAutreMetier = Some(false)
        )
        .avecNumeroTelephone(numeroTelephone = Some(commande.numeroTelephone))
        .build

      // When
      val result = candidat.modifierCriteres(commande.copy(
        rechercheAutreMetier = true
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si etreContacteParOrganismeFormation a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecCriteresRecherche(
          etreContacteParOrganismeFormation = Some(false)
        )
        .avecNumeroTelephone(numeroTelephone = Some(commande.numeroTelephone))
        .build

      // When
      val result = candidat.modifierCriteres(commande.copy(
        etreContacteParOrganismeFormation = true
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si etreContacteParAgenceInterim a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecCriteresRecherche(
          etreContacteParAgenceInterim = Some(false)
        )
        .avecNumeroTelephone(numeroTelephone = Some(commande.numeroTelephone))
        .build

      // When
      val result = candidat.modifierCriteres(commande.copy(
        etreContacteParAgenceInterim = true
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si rayonRecherche a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecCriteresRecherche(
          rayonRecherche = Some(RayonRecherche.MAX_30)
        )
        .avecNumeroTelephone(numeroTelephone = Some(commande.numeroTelephone))
        .build

      // When
      val result = candidat.modifierCriteres(commande.copy(
        rayonRecherche = RayonRecherche.MAX_50
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si un métier a été ajouté" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecCriteresRecherche(
          metiersRecherches = Some(Set.empty)
        )
        .avecNumeroTelephone(numeroTelephone = Some(commande.numeroTelephone))
        .build

      // When
      val result = candidat.modifierCriteres(commande.copy(
        metiersRecherches = commande.metiersRecherches + CodeROME("H3203")
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si un métier a été retiré" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecCriteresRecherche(
          metiersRecherches = Some(Set(CodeROME("H3203")))
        )
        .avecNumeroTelephone(numeroTelephone = Some(commande.numeroTelephone))
        .build

      // When
      val result = candidat.modifierCriteres(commande.copy(
        metiersRecherches = commande.metiersRecherches - CodeROME("H3203")
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement contenant les critères modifiés" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecNumeroTelephone(numeroTelephone = Some(commande.numeroTelephone))
        .build

      // When
      val result = candidat.modifierCriteres(commande)

      // Then
      val event = result.head.asInstanceOf[CriteresRechercheModifiesEvent]
      event.candidatId mustBe commande.id
      event.rechercheAutreMetier mustBe commande.rechercheAutreMetier
      event.rechercheMetierEvalue mustBe commande.rechercheMetierEvalue
      event.etreContacteParAgenceInterim mustBe commande.etreContacteParAgenceInterim
      event.etreContacteParOrganismeFormation mustBe commande.etreContacteParOrganismeFormation
      event.rayonRecherche mustBe commande.rayonRecherche
      event.metiersRecherches mustBe commande.metiersRecherches
    }
    "générer un événement lorsque le candidat est inscrit et que le numéro de téléphone change" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecCriteresRecherche(
          rechercheMetierEvalue = Some(commande.rechercheMetierEvalue),
          rechercheAutreMetier = Some(commande.rechercheAutreMetier),
          metiersRecherches = Some(commande.metiersRecherches),
          etreContacteParAgenceInterim = Some(commande.etreContacteParAgenceInterim),
          etreContacteParOrganismeFormation = Some(commande.etreContacteParOrganismeFormation),
          rayonRecherche = Some(commande.rayonRecherche)
        )
        .avecNumeroTelephone(numeroTelephone = Some(NumeroTelephone("0134767892")))
        .build

      // When
      val result = candidat.modifierCriteres(commande.copy(
        numeroTelephone = NumeroTelephone("0234567890")
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement contenant le numero de téléphone" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecCriteresRecherche(
          rechercheMetierEvalue = Some(commande.rechercheMetierEvalue),
          rechercheAutreMetier = Some(commande.rechercheAutreMetier),
          metiersRecherches = Some(commande.metiersRecherches),
          etreContacteParAgenceInterim = Some(commande.etreContacteParAgenceInterim),
          etreContacteParOrganismeFormation = Some(commande.etreContacteParOrganismeFormation),
          rayonRecherche = Some(commande.rayonRecherche)
        )
        .build

      // When
      val result = candidat.modifierCriteres(commande)

      // Then
      val event = result.head.asInstanceOf[NumeroTelephoneModifieEvent]
      event.candidatId mustBe commande.id
      event.numeroTelephone mustBe commande.numeroTelephone
    }
  }

}
