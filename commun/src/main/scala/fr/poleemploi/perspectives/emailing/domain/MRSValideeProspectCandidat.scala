package fr.poleemploi.perspectives.emailing.domain

import java.time.LocalDate

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.metier.domain.Metier

case class MRSValideeProspectCandidat(nom: Nom,
                                      prenom: Prenom,
                                      email: Email,
                                      genre: Genre,
                                      codeDepartement: CodeDepartement,
                                      metier: Metier,
                                      dateEvaluation: LocalDate)