package fr.poleemploi.perspectives.emailing.domain

import fr.poleemploi.perspectives.candidat.CandidatId

case class MiseAJourCVCandidat(candidatId: CandidatId, possedeCV: Boolean)
