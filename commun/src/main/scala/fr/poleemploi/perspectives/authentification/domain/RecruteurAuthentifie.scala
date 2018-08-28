package fr.poleemploi.perspectives.authentification.domain

import fr.poleemploi.perspectives.recruteur.RecruteurId

case class RecruteurAuthentifie(recruteurId: RecruteurId,
                                nom: String,
                                prenom: String)
