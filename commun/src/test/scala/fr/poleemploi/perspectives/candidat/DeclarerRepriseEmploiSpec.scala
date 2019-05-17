package fr.poleemploi.perspectives.candidat

import java.util.UUID

import fr.poleemploi.perspectives.conseiller.ConseillerId
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}

class DeclarerRepriseEmploiSpec extends WordSpec with MustMatchers with MockitoSugar {

  val candidatBuilder = new CandidatBuilder
  val conseillerId: ConseillerId = ConseillerId(UUID.randomUUID().toString)

  val commande: DeclarerRepriseEmploiParConseillerCommand =
    DeclarerRepriseEmploiParConseillerCommand(
      id = candidatBuilder.candidatId,
      conseillerId = conseillerId
    )

  "declarerRepriseEmploiParConseiller" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val ex = intercept[IllegalStateException] {
        candidat.declarerRepriseEmploiParConseiller(commande)
      }

      // Then
      ex.getMessage mustBe s"Le candidat ${candidat.id.value} avec le statut NOUVEAU ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
    }
    "renvoyer une erreur lorsque le candidat n'est pas en recherche d'emploi (reprise déjà déclarée)" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecRepriseEmploiDeclaree(conseillerId)
        .build

      // When
      val ex = intercept[IllegalArgumentException] {
        candidat.declarerRepriseEmploiParConseiller(commande)
      }

      // Then
      ex.getMessage must endWith(s"Le candidat ${candidat.id.value} n'est pas en recherche d'emploi")
    }
    "renvoyer une erreur lorsque le candidat n'est pas en recherche d'emploi (il a modifier ces critères lui même)" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecVisibiliteRecruteur(
          contactRecruteur = Some(false),
          contactFormation = Some(false)
        ).build

      // When
      val ex = intercept[IllegalArgumentException] {
        candidat.declarerRepriseEmploiParConseiller(commande)
      }

      // Then
      ex.getMessage must endWith(s"Le candidat ${candidat.id.value} n'est pas en recherche d'emploi")
    }
    "générer un événement lorsque la reprise d'emploi est déclarée" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When
      val result = candidat.declarerRepriseEmploiParConseiller(commande)

      // Then
      result.size mustBe 1
    }
    "générer un événement contenant les informations" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When
      val result = candidat.declarerRepriseEmploiParConseiller(commande)

      // Then
      val event = result.head.asInstanceOf[RepriseEmploiDeclareeParConseillerEvent]
      event.candidatId mustBe commande.id
      event.conseillerId mustBe conseillerId
    }
  }

}
