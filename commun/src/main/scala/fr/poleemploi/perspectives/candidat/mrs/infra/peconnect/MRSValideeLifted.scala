package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.time.LocalDate

import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME}
import slick.lifted.Rep

case class MRSValideeLifted(codeROME: Rep[CodeROME],
                            codeDepartement: Rep[CodeDepartement],
                            dateEvaluation: Rep[LocalDate])
