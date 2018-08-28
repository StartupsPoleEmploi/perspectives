package fr.poleemploi.perspectives.authentification.domain

import fr.poleemploi.perspectives.candidat.CandidatId

case class CandidatAuthentifie(candidatId: CandidatId,
                               nom: String,
                               prenom: String)
