package fr.poleemploi.perspectives.emailing.infra.mailjet

import fr.poleemploi.perspectives.recruteur.RecruteurId

case class RecruteurMailjet(recruteurId: RecruteurId,
                            mailjetContactId: MailjetContactId,
                            email: String)
