package fr.poleemploi.perspectives.domain.emailing

import fr.poleemploi.perspectives.domain.Genre
import fr.poleemploi.perspectives.domain.recruteur.RecruteurId

case class RecruteurInscrit(recruteurId: RecruteurId,
                            nom: String,
                            prenom: String,
                            email: String,
                            genre: Genre)
