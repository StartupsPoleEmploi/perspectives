package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.time.LocalDate

import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}

class ReferentielMRSPEConnectSpec extends WordSpec
  with MustMatchers with MockitoSugar {

  "prioriserMRSDHAEValidees" should {
    "renvoyer une liste des MRS en priorisant les DHAE lorsqu'il existe une MRS non DHAE et une MRS DHAE avec le même code ROME et le même département" in {
      // Given
      val dateEvaluation = LocalDate.now()
      val mrsValidees = List(
        MRSValidee(CodeROME("A0001"), CodeDepartement("75"), dateEvaluation, isDHAE = false),
        MRSValidee(CodeROME("A0002"), CodeDepartement("75"), dateEvaluation, isDHAE = false),
        MRSValidee(CodeROME("A0003"), CodeDepartement("75"), dateEvaluation, isDHAE = false)
      )
      val mrsDHAEValidees = List(
        MRSValidee(CodeROME("A0002"), CodeDepartement("75"), dateEvaluation, isDHAE = true),
        MRSValidee(CodeROME("A0004"), CodeDepartement("75"), dateEvaluation, isDHAE = true)
      )

      // When
      val result = ReferentielMRSPEConnect.prioriserMRSDHAEValidees(mrsValidees, mrsDHAEValidees)

      // Then
      result must contain theSameElementsAs List(
        MRSValidee(CodeROME("A0001"), CodeDepartement("75"), dateEvaluation, isDHAE = false),
        MRSValidee(CodeROME("A0002"), CodeDepartement("75"), dateEvaluation, isDHAE = true),
        MRSValidee(CodeROME("A0003"), CodeDepartement("75"), dateEvaluation, isDHAE = false),
        MRSValidee(CodeROME("A0004"), CodeDepartement("75"), dateEvaluation, isDHAE = true)
      )
    }
  }
}
