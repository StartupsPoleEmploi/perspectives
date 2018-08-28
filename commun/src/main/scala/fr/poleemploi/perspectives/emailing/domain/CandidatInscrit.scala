package fr.poleemploi.perspectives.emailing.domain

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.Genre

case class CandidatInscrit(candidatId: CandidatId,
                           nom: String,
                           prenom: String,
                           email: String,
                           genre: Genre,
                           cv: Boolean)
