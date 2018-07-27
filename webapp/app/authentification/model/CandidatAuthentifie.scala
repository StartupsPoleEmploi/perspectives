package authentification.model

import fr.poleemploi.perspectives.domain.candidat.CandidatId

case class CandidatAuthentifie(candidatId: CandidatId,
                               nom: String,
                               prenom: String)