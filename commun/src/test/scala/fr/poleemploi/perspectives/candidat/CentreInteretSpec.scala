package fr.poleemploi.perspectives.candidat

import org.scalatest.{MustMatchers, WordSpec}

class CentreInteretSpec extends WordSpec with MustMatchers {

  "apply" should {
    "ajouter une majuscule pour la premiere lettre" in {
      // Given

      // When
      val centreInteret = CentreInteret("jeux de société")

      // Then
      centreInteret.value mustBe "Jeux de société"
    }
    "ne pas modifier le reste car peut contenir des acronymes" in {
      // Given

      // When
      val centreInteret = CentreInteret("VTT")

      // Then
      centreInteret.value mustBe "VTT"
    }
  }
}