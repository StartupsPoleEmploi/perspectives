package fr.poleemploi.perspectives.candidat.mrs.domain

import java.time.LocalDate

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.CodeROME

case class MRSValidee(codeROME: CodeROME,
                      dateEvaluation: LocalDate)

case class MRSValideeCandidat(candidatId: CandidatId,
                              codeROME: CodeROME,
                              dateEvaluation: LocalDate)
