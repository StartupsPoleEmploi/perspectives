package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.time.LocalDate

import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME}
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId

/**
  * DHAE (Des habiletés à l'emploi), est un projet de PôleEmploi afin de valider des candidats sur des CodeROME mais sans
  * employeur derrière comme pour une MRS classique.
  */
case class MRSDHAEValideePEConnect(peConnectId: PEConnectId,
                                   codeROME: CodeROME,
                                   codeDepartement: CodeDepartement,
                                   dateEvaluation: LocalDate)