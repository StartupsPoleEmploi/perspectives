package fr.poleemploi.perspectives.prospect.infra.sql

import java.time.LocalDate

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId

case class ProspectCandidatRecord(peConnectId: PEConnectId,
                                  identifiantLocal: IdentifiantLocal,
                                  codeNeptune: Option[CodeNeptune],
                                  nom: Nom,
                                  prenom: Prenom,
                                  email: Email,
                                  genre: Genre,
                                  codeDepartement: CodeDepartement,
                                  codeRomeMrs: CodeROME,
                                  metierMrs: String,
                                  dateEvaluationMrs: LocalDate)
