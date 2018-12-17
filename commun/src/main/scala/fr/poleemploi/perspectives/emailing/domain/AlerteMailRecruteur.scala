package fr.poleemploi.perspectives.emailing.domain

import fr.poleemploi.perspectives.commun.domain._

case class AlerteMailRecruteur(email: Email,
                               sujet: String,
                               recapitulatifInscriptions: String,
                               lienConnexion: String)
