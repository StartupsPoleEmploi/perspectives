package fr.poleemploi.perspectives.candidat.mrs.domain

import java.time.LocalDate

import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME}

case class MRSValidee(codeROME: CodeROME,
                      codeDepartement: CodeDepartement,
                      dateEvaluation: LocalDate,
                      isDHAE: Boolean)
