package fr.poleemploi.perspectives.domain.authentification

import fr.poleemploi.perspectives.domain.recruteur.RecruteurId

case class RecruteurAuthentifie(recruteurId: RecruteurId,
                                nom: String,
                                prenom: String)
