package fr.poleemploi.perspectives.domain.candidat.mrs.infra

import java.time.LocalDate

import fr.poleemploi.perspectives.domain.authentification.infra.peconnect.PEConnectId

case class MRSValideeCandidatPEConnect(peConnectId: PEConnectId,
                                       codeMetier: String,
                                       dateEvaluation: LocalDate)
