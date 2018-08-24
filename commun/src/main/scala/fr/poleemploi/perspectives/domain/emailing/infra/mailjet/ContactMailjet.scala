package fr.poleemploi.perspectives.domain.emailing.infra.mailjet

import fr.poleemploi.perspectives.domain.candidat.CandidatId
import fr.poleemploi.perspectives.domain.recruteur.RecruteurId

case class CandidatMailjet(candidatId: CandidatId,
                           mailjetContactId: MailjetContactId,
                           email: String)

case class RecruteurMailjet(recruteurId: RecruteurId,
                            mailjetContactId: MailjetContactId,
                            email: String)

