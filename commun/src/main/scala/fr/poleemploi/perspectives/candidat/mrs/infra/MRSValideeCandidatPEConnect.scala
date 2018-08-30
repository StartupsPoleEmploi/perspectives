package fr.poleemploi.perspectives.candidat.mrs.infra

import java.time.LocalDate

import fr.poleemploi.perspectives.commun.domain.CodeROME
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId

case class MRSValideeCandidatPEConnect(peConnectId: PEConnectId,
                                       codeROME: CodeROME,
                                       dateEvaluation: LocalDate)
