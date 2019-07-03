package fr.poleemploi.perspectives.emailing.domain

import java.time.LocalDate

import fr.poleemploi.perspectives.metier.domain.Metier

case class MRSValideeCandidat(metier: Metier,
                              dateEvaluation: LocalDate)