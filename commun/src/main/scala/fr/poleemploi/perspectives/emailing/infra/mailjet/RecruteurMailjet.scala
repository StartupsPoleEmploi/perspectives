package fr.poleemploi.perspectives.emailing.infra.mailjet

import fr.poleemploi.perspectives.commun.domain.Email
import fr.poleemploi.perspectives.recruteur.RecruteurId

case class RecruteurMailjet(recruteurId: RecruteurId,
                            mailjetContactId: MailjetContactId,
                            email: Email)
