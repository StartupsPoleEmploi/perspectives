package fr.poleemploi.perspectives.candidat.mrs.domain

import java.time.LocalDate

import fr.poleemploi.perspectives.commun.domain.CodeROME

case class MRSValidee(codeROME: CodeROME,
                      dateEvaluation: LocalDate)
