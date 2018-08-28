package fr.poleemploi.perspectives.candidat

import java.time.LocalDate

import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}

class AjouterMRSValideeSpec extends WordSpec with MustMatchers with MockitoSugar {

  val candidatBuilder = new CandidatBuilder

  val mrsValidee = MRSValidee(
    codeMetier = "H083",
    dateEvaluation = LocalDate.now()
  )

  val commande: AjouterMRSValideesCommand =
    AjouterMRSValideesCommand(
      id = candidatBuilder.candidatId,
      mrsValidees = List(mrsValidee)
    )

  "ajouterMRSValidee" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val ex = intercept[RuntimeException] {
        candidat.ajouterMRSValidee(commande)
      }

      // Then
      ex.getMessage mustBe s"Le candidat ${candidat.id.value} n'est pas encore inscrit"
    }
    "renvoyer une erreur lorsque le candidat a déjà passé la MRS" in {
      // Given
      val candidat = candidatBuilder.avecInscription().avecMRSValidee(mrsValidee).build

      // When
      val ex = intercept[RuntimeException] {
        candidat.ajouterMRSValidee(commande)
      }

      // Then
      ex.getMessage must startWith(s"Le candidat ${candidat.id.value} a déjà validé les MRS suivantes")
    }
    "ne pas générer d'événement lorsqu'aucune MRS n'est à ajouter" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When
      val result = candidat.ajouterMRSValidee(commande.copy(
        mrsValidees = Nil
      ))

      // Then
      result.isEmpty mustBe true
    }
    "générer un événement lorsque la MRS est ajoutée" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When
      val result = candidat.ajouterMRSValidee(commande)

      // Then
      result.size mustBe 1
    }
    "générer autant d'événément que le nombre de MRS à ajouter" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When
      val result = candidat.ajouterMRSValidee(commande.copy(
        mrsValidees = List(MRSValidee(
          codeMetier = "1",
          dateEvaluation = LocalDate.now()
        ), MRSValidee(
          codeMetier = "2",
          dateEvaluation = LocalDate.now()
        ))
      ))

      // Then
      result.size mustBe 2
    }
    "générer un événement contenant la MRS ajoutée" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When
      val result = candidat.ajouterMRSValidee(commande)

      // Then
      val event = result.head.asInstanceOf[MRSAjouteeEvent]
      event.candidatId mustBe commande.id
      event.metier mustBe commande.mrsValidees.head.codeMetier
      event.dateEvaluation mustBe commande.mrsValidees.head.dateEvaluation
    }
  }

}
