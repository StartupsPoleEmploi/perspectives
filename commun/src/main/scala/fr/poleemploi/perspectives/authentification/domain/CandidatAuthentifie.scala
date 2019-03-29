package fr.poleemploi.perspectives.authentification.domain

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{Nom, Prenom}

case class CandidatAuthentifie(candidatId: CandidatId,
                               nom: Nom,
                               prenom: Prenom)
