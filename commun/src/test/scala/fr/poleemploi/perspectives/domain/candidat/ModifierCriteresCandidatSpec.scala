package fr.poleemploi.perspectives.domain.candidat

import java.util.UUID

import fr.poleemploi.perspectives.domain.Metier
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}

class ModifierCriteresCandidatSpec extends WordSpec with MustMatchers with MockitoSugar {

  val candidatId: CandidatId = CandidatId(UUID.randomUUID().toString)

  val modifierCriteresCommande: ModifierCriteresRechercheCommand =
    ModifierCriteresRechercheCommand(
      id = candidatId,
      rechercheAutreMetier = true,
      rechercheMetierEvalue = false,
      metiersRecherches = Set.empty,
      etreContacteParAgenceInterim = true,
      etreContacteParOrganismeFormation = true,
      rayonRecherche = 10
    )

  val criteresRechercheModifieEvent =
    CriteresRechercheModifiesEvent(
      rechercheAutreMetier = modifierCriteresCommande.rechercheAutreMetier,
      rechercheMetierEvalue = modifierCriteresCommande.rechercheMetierEvalue,
      etreContacteParAgenceInterim = modifierCriteresCommande.etreContacteParAgenceInterim,
      etreContacteParOrganismeFormation = modifierCriteresCommande.etreContacteParOrganismeFormation,
      listeMetiersRecherches = modifierCriteresCommande.metiersRecherches.map(_.code),
      rayonRecherche = modifierCriteresCommande.rayonRecherche
    )

  "modifierCriteres" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = Nil
      )

      // When
      val ex = intercept[RuntimeException] {
        candidat.modifierCriteres(modifierCriteresCommande)
      }

      // Then
      ex.getMessage mustBe s"Le candidat ${candidat.id.value} n'est pas encore inscrit"
    }
    "ne pas générer d'événement si aucun critère n'a été modifié" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(mock[CandidatInscrisEvent], criteresRechercheModifieEvent)
      )

      // When
      val result = candidat.modifierCriteres(modifierCriteresCommande)

      // Then
      result.isEmpty mustBe true
    }
    "générer un événement si un critère a été saisi pour la premiere fois" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(mock[CandidatInscrisEvent])
      )

      // When
      val result = candidat.modifierCriteres(modifierCriteresCommande)

      // Then
      result.size mustBe 1
    }
    "générer un événement si rechercheMetierEvalue a été modifié" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(mock[CandidatInscrisEvent], criteresRechercheModifieEvent.copy(
          rechercheMetierEvalue = false
        ))
      )

      // When
      val result = candidat.modifierCriteres(modifierCriteresCommande.copy(
        rechercheMetierEvalue = true
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si rechercheAutreMetier a été modifié" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(mock[CandidatInscrisEvent], criteresRechercheModifieEvent.copy(
          rechercheAutreMetier = false
        ))
      )

      // When
      val result = candidat.modifierCriteres(modifierCriteresCommande.copy(
        rechercheAutreMetier = true
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si etreContacteParOrganismeFormation a été modifié" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(mock[CandidatInscrisEvent], criteresRechercheModifieEvent.copy(
          etreContacteParOrganismeFormation = false
        ))
      )

      // When
      val result = candidat.modifierCriteres(modifierCriteresCommande.copy(
        etreContacteParOrganismeFormation = true
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si etreContacteParAgenceInterim a été modifié" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(mock[CandidatInscrisEvent], criteresRechercheModifieEvent.copy(
          etreContacteParAgenceInterim = false
        ))
      )

      // When
      val result = candidat.modifierCriteres(modifierCriteresCommande.copy(
        etreContacteParAgenceInterim = true
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si rayonRecherche a été modifié" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(mock[CandidatInscrisEvent], criteresRechercheModifieEvent.copy(
          rayonRecherche = 30
        ))
      )

      // When
      val result = candidat.modifierCriteres(modifierCriteresCommande.copy(
        rayonRecherche = 50
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si un métier a été ajouté" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(mock[CandidatInscrisEvent], criteresRechercheModifieEvent.copy(
          listeMetiersRecherches = Set.empty
        ))
      )

      // When
      val result = candidat.modifierCriteres(modifierCriteresCommande.copy(
        metiersRecherches = modifierCriteresCommande.metiersRecherches + Metier.SERVICE
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si un métier a été retiré" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(mock[CandidatInscrisEvent], criteresRechercheModifieEvent.copy(
          listeMetiersRecherches = Set(Metier.SERVICE.code)
        ))
      )

      // When
      val result = candidat.modifierCriteres(modifierCriteresCommande.copy(
        metiersRecherches = modifierCriteresCommande.metiersRecherches - Metier.SERVICE
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement contenant les critères modifiés" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(mock[CandidatInscrisEvent])
      )

      // When
      val result = candidat.modifierCriteres(modifierCriteresCommande)

      // Then
      val event = result.head.asInstanceOf[CriteresRechercheModifiesEvent]
      event.rechercheAutreMetier mustBe modifierCriteresCommande.rechercheAutreMetier
      event.rechercheMetierEvalue mustBe modifierCriteresCommande.rechercheMetierEvalue
      event.etreContacteParAgenceInterim mustBe modifierCriteresCommande.etreContacteParAgenceInterim
      event.etreContacteParOrganismeFormation mustBe modifierCriteresCommande.etreContacteParOrganismeFormation
      event.rayonRecherche mustBe modifierCriteresCommande.rayonRecherche
      event.listeMetiersRecherches mustBe modifierCriteresCommande.metiersRecherches.map(_.code)
    }
  }

}
