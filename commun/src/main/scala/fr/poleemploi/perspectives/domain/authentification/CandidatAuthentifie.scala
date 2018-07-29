package fr.poleemploi.perspectives.domain.authentification

import fr.poleemploi.perspectives.domain.candidat.CandidatId

case class CandidatAuthentifie(candidatId: CandidatId,
                               nom: String,
                               prenom: String)
