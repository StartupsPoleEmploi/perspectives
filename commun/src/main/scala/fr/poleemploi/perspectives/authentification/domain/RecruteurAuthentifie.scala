package fr.poleemploi.perspectives.authentification.domain

import fr.poleemploi.perspectives.commun.domain.{Nom, Prenom}
import fr.poleemploi.perspectives.recruteur.RecruteurId

case class RecruteurAuthentifie(recruteurId: RecruteurId,
                                nom: Nom,
                                prenom: Prenom)
