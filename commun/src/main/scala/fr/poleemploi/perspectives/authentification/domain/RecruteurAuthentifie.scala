package fr.poleemploi.perspectives.authentification.domain

import fr.poleemploi.perspectives.commun.domain.{Email, Nom, Prenom}
import fr.poleemploi.perspectives.recruteur.{RecruteurId, TypeRecruteur}

case class RecruteurAuthentifie(recruteurId: RecruteurId,
                                nom: Nom,
                                prenom: Prenom,
                                email: Email,
                                typeRecruteur: Option[TypeRecruteur],
                                certifie: Boolean)
