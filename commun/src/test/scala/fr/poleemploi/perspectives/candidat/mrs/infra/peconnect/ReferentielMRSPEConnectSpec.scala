package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.time.LocalDate

import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, MustMatchers}

class ReferentielMRSPEConnectSpec extends AsyncWordSpec with MustMatchers with MockitoSugar {
  "ReferentielMRSPEConnect" should {
    "dédoublonner les MRS et DHAE par rome et département" in {
      val dateEvaluation = LocalDate.now()
      val mrsSeq = Seq(
        MRSValidee(CodeROME("A0001"), CodeDepartement("75"), dateEvaluation, isDHAE = false),
        MRSValidee(CodeROME("A0002"), CodeDepartement("75"), dateEvaluation, isDHAE = false),
        MRSValidee(CodeROME("A0003"), CodeDepartement("75"), dateEvaluation, isDHAE = false),
      )
      val dhaeSeq = Seq(
        MRSValidee(CodeROME("A0001"), CodeDepartement("75"), dateEvaluation, isDHAE = true),
        MRSValidee(CodeROME("A0002"), CodeDepartement("75"), dateEvaluation, isDHAE = true),
        MRSValidee(CodeROME("A0003"), CodeDepartement("75"), dateEvaluation, isDHAE = true),
      )

      ReferentielMRSPEConnect.mergeAndRemoveDuplicate(mrsSeq, dhaeSeq)
        .map(m => (m.codeROME, m.codeDepartement)) must (contain allOf(
        (CodeROME("A0001"), CodeDepartement("75")),
        (CodeROME("A0002"), CodeDepartement("75")),
        (CodeROME("A0003"), CodeDepartement("75"))
      ) and have size 3)
    }
  }
}
