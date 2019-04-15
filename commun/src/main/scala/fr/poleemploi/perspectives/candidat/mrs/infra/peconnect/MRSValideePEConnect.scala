package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.time.LocalDate

import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME}
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId

case class MRSValideePEConnect(peConnectId: PEConnectId,
                               codeROME: CodeROME,
                               codeDepartement: CodeDepartement,
                               dateEvaluation: LocalDate)
