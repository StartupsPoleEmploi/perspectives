package fr.poleemploi.perspectives.prospect.domain

import java.time.LocalDate

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.metier.domain.Metier

case class ProspectCandidat(peConnectId: PEConnectId,
                            identifiantLocal: IdentifiantLocal,
                            codeNeptune: Option[CodeNeptune],
                            nom: Nom,
                            prenom: Prenom,
                            email: Email,
                            genre: Genre,
                            codeDepartement: CodeDepartement,
                            metier: Metier,
                            dateEvaluation: LocalDate)
