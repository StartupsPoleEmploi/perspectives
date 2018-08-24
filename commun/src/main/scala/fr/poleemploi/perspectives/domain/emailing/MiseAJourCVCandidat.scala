package fr.poleemploi.perspectives.domain.emailing

import fr.poleemploi.perspectives.domain.candidat.CandidatId

case class MiseAJourCVCandidat(candidatId: CandidatId, possedeCV: Boolean)
