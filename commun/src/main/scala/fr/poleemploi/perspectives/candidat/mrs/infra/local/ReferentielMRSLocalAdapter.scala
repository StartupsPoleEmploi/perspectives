package fr.poleemploi.perspectives.candidat.mrs.infra.local

import java.time.LocalDate

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.mrs.domain.{MRSValidee, ReferentielMRS}
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME}

import scala.concurrent.Future

class ReferentielMRSLocalAdapter extends ReferentielMRS {

  override def mrsValidees(candidatId: CandidatId): Future[List[MRSValidee]] =
    Future.successful(List(
      MRSValidee(
        codeROME = CodeROME("A1401"),
        codeDepartement = CodeDepartement("85"),
        dateEvaluation = LocalDate.now(),
        isDHAE = false
      ),
      MRSValidee(
        codeROME = CodeROME("D1106"),
        codeDepartement = CodeDepartement("85"),
        dateEvaluation = LocalDate.now(),
        isDHAE = false
      ),
      MRSValidee(
        codeROME = CodeROME("D1105"),
        codeDepartement = CodeDepartement("85"),
        dateEvaluation = LocalDate.now(),
        isDHAE = false
      )
    ))

}
