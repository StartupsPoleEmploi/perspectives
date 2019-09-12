package fr.poleemploi.perspectives.emailing.domain

import java.time.LocalDate

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.metier.domain.Metier

case class MRSDHAEValideeProspectCandidat(peConnectId: PEConnectId,
                                          override val codeDepartement: CodeDepartement,
                                          override val dateEvaluation: LocalDate,
                                          override val nom: Nom,
                                          override val prenom: Prenom,
                                          override val email: Email,
                                          override val genre: Genre,
                                          override val metier: Metier) extends MRSProspectCandidat

