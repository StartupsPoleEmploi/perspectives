package fr.poleemploi.perspectives.candidat.activite.infra

import java.time.LocalDate

import fr.poleemploi.perspectives.candidat.CandidatId

case class DisponibiliteCandidat(candidatId: CandidatId,
                                 dateDernierEnvoiMail: LocalDate)
