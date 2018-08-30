package fr.poleemploi.perspectives.emailing.domain

import fr.poleemploi.perspectives.commun.domain.{Email, Genre}
import fr.poleemploi.perspectives.recruteur.RecruteurId

case class RecruteurInscrit(recruteurId: RecruteurId,
                            nom: String,
                            prenom: String,
                            email: Email,
                            genre: Genre)
