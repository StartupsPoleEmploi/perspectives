package fr.poleemploi.perspectives.domain.candidat

import java.util.UUID

import fr.poleemploi.perspectives.domain.{Genre, Metier}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class ModifierCriteresCandidatSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val candidatId: CandidatId = CandidatId(UUID.randomUUID().toString)

  val commande: ModifierCriteresRechercheCommand =
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
      rechercheAutreMetier = commande.rechercheAutreMetier,
      rechercheMetierEvalue = commande.rechercheMetierEvalue,
      etreContacteParAgenceInterim = commande.etreContacteParAgenceInterim,
      etreContacteParOrganismeFormation = commande.etreContacteParOrganismeFormation,
      metiersRecherches = commande.metiersRecherches,
      rayonRecherche = commande.rayonRecherche
    )

  var candidatInscrisEvent: CandidatInscrisEvent = _

  before {
    candidatInscrisEvent = mock[CandidatInscrisEvent]
    when(candidatInscrisEvent.genre) thenReturn Some(Genre.HOMME)
  }

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
        candidat.modifierCriteres(commande)
      }

      // Then
      ex.getMessage mustBe s"Le candidat ${candidat.id.value} n'est pas encore inscrit"
    }
    "ne pas générer d'événement si aucun critère n'a été modifié" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, criteresRechercheModifieEvent)
      )

      // When
      val result = candidat.modifierCriteres(commande)

      // Then
      result.isEmpty mustBe true
    }
    "générer un événement si un critère a été saisi pour la premiere fois" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent)
      )

      // When
      val result = candidat.modifierCriteres(commande)

      // Then
      result.size mustBe 1
    }
    "générer un événement si rechercheMetierEvalue a été modifié" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, criteresRechercheModifieEvent.copy(
          rechercheMetierEvalue = false
        ))
      )

      // When
      val result = candidat.modifierCriteres(commande.copy(
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
        events = List(candidatInscrisEvent, criteresRechercheModifieEvent.copy(
          rechercheAutreMetier = false
        ))
      )

      // When
      val result = candidat.modifierCriteres(commande.copy(
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
        events = List(candidatInscrisEvent, criteresRechercheModifieEvent.copy(
          etreContacteParOrganismeFormation = false
        ))
      )

      // When
      val result = candidat.modifierCriteres(commande.copy(
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
        events = List(candidatInscrisEvent, criteresRechercheModifieEvent.copy(
          etreContacteParAgenceInterim = false
        ))
      )

      // When
      val result = candidat.modifierCriteres(commande.copy(
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
        events = List(candidatInscrisEvent, criteresRechercheModifieEvent.copy(
          rayonRecherche = 30
        ))
      )

      // When
      val result = candidat.modifierCriteres(commande.copy(
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
        events = List(candidatInscrisEvent, criteresRechercheModifieEvent.copy(
          metiersRecherches = Set.empty
        ))
      )

      // When
      val result = candidat.modifierCriteres(commande.copy(
        metiersRecherches = commande.metiersRecherches + Metier.SERVICE
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement si un métier a été retiré" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, criteresRechercheModifieEvent.copy(
          metiersRecherches = Set(Metier.SERVICE)
        ))
      )

      // When
      val result = candidat.modifierCriteres(commande.copy(
        metiersRecherches = commande.metiersRecherches - Metier.SERVICE
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement contenant les critères modifiés" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent)
      )

      // When
      val result = candidat.modifierCriteres(commande)

      // Then
      val event = result.head.asInstanceOf[CriteresRechercheModifiesEvent]
      event.rechercheAutreMetier mustBe commande.rechercheAutreMetier
      event.rechercheMetierEvalue mustBe commande.rechercheMetierEvalue
      event.etreContacteParAgenceInterim mustBe commande.etreContacteParAgenceInterim
      event.etreContacteParOrganismeFormation mustBe commande.etreContacteParOrganismeFormation
      event.rayonRecherche mustBe commande.rayonRecherche
      event.metiersRecherches mustBe commande.metiersRecherches
    }
  }

}
