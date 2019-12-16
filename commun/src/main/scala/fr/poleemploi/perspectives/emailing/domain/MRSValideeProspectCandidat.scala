package fr.poleemploi.perspectives.emailing.domain

import java.time.LocalDate

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.metier.domain.Metier

case class MRSValideeProspectCandidat(override val peConnectId: PEConnectId,
                                      override val identifiantLocal: IdentifiantLocal,
                                      override val codeNeptune: CodeNeptune,
                                      override val nom: Nom,
                                      override val prenom: Prenom,
                                      override val email: Email,
                                      override val genre: Genre,
                                      override val codeDepartement: CodeDepartement,
                                      override val metier: Metier,
                                      override val dateEvaluation: LocalDate) extends MRSProspectCandidat
