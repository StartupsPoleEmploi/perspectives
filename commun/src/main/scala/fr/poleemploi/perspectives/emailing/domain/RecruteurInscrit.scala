package fr.poleemploi.perspectives.emailing.domain

import fr.poleemploi.perspectives.commun.domain.{Email, Genre, Nom, Prenom}
import fr.poleemploi.perspectives.recruteur.RecruteurId

case class RecruteurInscrit(recruteurId: RecruteurId,
                            nom: Nom,
                            prenom: Prenom,
                            email: Email,
                            genre: Genre)
