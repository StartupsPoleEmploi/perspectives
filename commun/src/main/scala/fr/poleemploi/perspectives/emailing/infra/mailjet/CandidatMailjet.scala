package fr.poleemploi.perspectives.emailing.infra.mailjet

import fr.poleemploi.perspectives.candidat.CandidatId

case class CandidatMailjet(candidatId: CandidatId,
                           mailjetContactId: MailjetContactId,
                           email: String)
