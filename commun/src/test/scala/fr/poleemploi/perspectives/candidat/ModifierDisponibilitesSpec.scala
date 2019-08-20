package fr.poleemploi.perspectives.candidat

import java.time.LocalDate

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, MustMatchers}

class ModifierDisponibilitesSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar {

  val candidatBuilder = new CandidatBuilder

  val prochaineDisponibilite: LocalDate =
    LocalDate.of(2019, 4, 11)

  val commande: ModifierDisponibilitesCommand =
    ModifierDisponibilitesCommand(
      id = candidatBuilder.candidatId,
      candidatEnRecherche = false,
      emploiTrouveGracePerspectives = false,
      prochaineDisponibilite = Some(prochaineDisponibilite)
    )

  "modifierDisponibilites" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val ex = intercept[IllegalStateException](
        candidat.modifierDisponibilites(commande)
      )

      // Then
      ex.getMessage mustBe s"Le candidat ${candidat.id.value} avec le statut NOUVEAU ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
    }
    "ne pas générer d'événement lorsque rien ne change" in {
      // Given
      val candidat = candidatInscritAvecDisponibilites(commande).build

      // When
      val result = candidat.modifierDisponibilites(commande)

      // Then
      result.isEmpty mustBe true
    }
    "générer un événement lorsque candidatEnRecherche est modifié" in {
      // Given
      val candidat = candidatInscritAvecDisponibilites(commande)
        .avecDisponibilites(
          candidatEnRecherche = Some(false)
        )
        .build

      // When
      val result = candidat.modifierDisponibilites(commande.copy(
        candidatEnRecherche = true
      ))

      // Then
      result.count(_.isInstanceOf[DisponibilitesModifieesEvent]) mustBe 1
    }
    "générer un événement lorsque emploiTrouveGracePerspectives est modifié" in {
      // Given
      val candidat = candidatInscritAvecDisponibilites(commande)
        .avecDisponibilites(
          emploiTrouveGracePerspectives = Some(false)
        )
        .build

      // When
      val result = candidat.modifierDisponibilites(commande.copy(
        emploiTrouveGracePerspectives = true
      ))

      // Then
      result.count(_.isInstanceOf[DisponibilitesModifieesEvent]) mustBe 1
    }
    "générer un événement lorsque la prochaine date de disponibilite est ajoutée" in {
      // Given
      val candidat = candidatInscritAvecDisponibilites(commande)
        .avecDisponibilites(
          prochaineDisponibilite = None
        )
        .build

      // When
      val result = candidat.modifierDisponibilites(commande.copy(
        prochaineDisponibilite = Some(prochaineDisponibilite)
      ))

      // Then
      result.count(_.isInstanceOf[DisponibilitesModifieesEvent]) mustBe 1
    }
    "générer un événement lorsque la prochaine date de disponibilite est supprimée" in {
      // Given
      val candidat = candidatInscritAvecDisponibilites(commande)
        .avecDisponibilites(
          prochaineDisponibilite = Some(prochaineDisponibilite)
        )
        .build

      // When
      val result = candidat.modifierDisponibilites(commande.copy(
        prochaineDisponibilite = None
      ))

      // Then
      result.count(_.isInstanceOf[DisponibilitesModifieesEvent]) mustBe 1
    }
    "générer un événement lorsque la prochaine date de disponibilite est modifiée" in {
      // Given
      val candidat = candidatInscritAvecDisponibilites(commande)
        .avecDisponibilites(
          prochaineDisponibilite = Some(prochaineDisponibilite)
        )
        .build

      // When
      val result = candidat.modifierDisponibilites(commande.copy(
        prochaineDisponibilite = Some(prochaineDisponibilite.plusDays(1))
      ))

      // Then
      result.count(_.isInstanceOf[DisponibilitesModifieesEvent]) mustBe 1
    }
    "générer un événement contenant les disponibilites" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .build

      // When
      val result = candidat.modifierDisponibilites(commande)

      // Then
      val event = result.head.asInstanceOf[DisponibilitesModifieesEvent]
      event.candidatId mustBe commande.id
      event.candidatEnRecherche mustBe commande.candidatEnRecherche
      event.emploiTrouveGracePerspectives mustBe commande.emploiTrouveGracePerspectives
      event.prochaineDisponibilite mustBe commande.prochaineDisponibilite
    }
  }

  private def candidatInscritAvecDisponibilites(commande: ModifierDisponibilitesCommand): CandidatBuilder =
    candidatBuilder
      .avecInscription()
      .avecDisponibilites(
        candidatEnRecherche = Some(commande.candidatEnRecherche),
        emploiTrouveGracePerspectives = Some(commande.emploiTrouveGracePerspectives),
        prochaineDisponibilite = commande.prochaineDisponibilite
      )
}
