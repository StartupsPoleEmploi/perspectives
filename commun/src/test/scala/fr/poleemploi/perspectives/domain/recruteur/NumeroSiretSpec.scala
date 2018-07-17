package fr.poleemploi.perspectives.domain.recruteur

import org.scalatest.{MustMatchers, WordSpec}

class NumeroSiretSpec extends WordSpec with MustMatchers {

  val numerosValides = List(
    "13000548100010",
    "00000000000018"
  )

  "from" should {
    "retourner None lorsque ce n'est pas un nombre" in {
      // When
      val result = NumeroSiret.from("chaine")

      // Then
      result mustBe None
    }
    "retourner None lorsque ce n'est pas un nombre qui fait 14 caracteres" in {
      // When
      val result = NumeroSiret.from("c" * 14)

      // Then
      result mustBe None
    }
    "retourner None lorsque c'est un nombre mais qui fait plus de 14 chiffres" in {
      // When
      val result = NumeroSiret.from("1234567891234567")

      // Then
      result mustBe None
    }
    "retourner None lorsque c'est un nombre mais qui fait moins de 14 chiffres" in {
      // When
      val result = NumeroSiret.from("12345")

      // Then
      result mustBe None
    }
    "retourner None lorsque c'est un nombre de 14 chiffres mais qui n'est pas un Siret" in {
      // When
      val result = NumeroSiret.from("12345678901234")

      // Then
      result mustBe None
    }
    "retourner Some lorsque c'est un numéro de Siret valide" in {
      // When & Then
      numerosValides.forall(n => NumeroSiret.from(n).isDefined) mustBe true
    }
  }
}
