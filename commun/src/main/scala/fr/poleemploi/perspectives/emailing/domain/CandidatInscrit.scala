package fr.poleemploi.perspectives.emailing.domain

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{Email, Genre}

case class CandidatInscrit(candidatId: CandidatId,
                           nom: String,
                           prenom: String,
                           email: Email,
                           genre: Genre,
                           cv: Boolean)
