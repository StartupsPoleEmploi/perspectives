package fr.poleemploi.perspectives.emailing.domain

import fr.poleemploi.perspectives.authentification.infra.autologin.JwtToken
import fr.poleemploi.perspectives.commun.domain.Email

case class EmailingCandidatJVR(email: Email,
                               autologinToken: JwtToken)
