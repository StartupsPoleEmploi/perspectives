package fr.poleemploi.perspectives.candidat.mrs.domain

import java.time.LocalDate

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME}

import scala.concurrent.Future

case class MRSValideeCandidat(candidatId: CandidatId,
                              codeROME: CodeROME,
                              codeDepartement: CodeDepartement,
                              dateEvaluation: LocalDate)

trait ImportMRS {

  def integrerMRSValidees: Future[Stream[MRSValideeCandidat]]
}
