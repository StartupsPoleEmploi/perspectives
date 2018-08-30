package fr.poleemploi.perspectives.emailing.infra.mailjet

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.Email

case class CandidatMailjet(candidatId: CandidatId,
                           mailjetContactId: MailjetContactId,
                           email: Email)
