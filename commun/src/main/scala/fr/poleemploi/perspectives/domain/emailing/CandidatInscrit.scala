package fr.poleemploi.perspectives.domain.emailing

import fr.poleemploi.perspectives.domain.Genre
import fr.poleemploi.perspectives.domain.candidat.CandidatId

case class CandidatInscrit(candidatId: CandidatId,
                           nom: String,
                           prenom: String,
                           email: String,
                           genre: Option[Genre],
                           cv: Boolean)
