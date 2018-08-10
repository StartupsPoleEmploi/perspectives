package fr.poleemploi.perspectives.projections.candidat

sealed trait ResultatRechercheCandidat {
  def nbCandidats: Int
}

case class ResultatRechercheCandidatParDateInscription(candidats: List[CandidatDto]) extends ResultatRechercheCandidat {

  override val nbCandidats: Int = candidats.size
}

case class ResultatRechercheCandidatParSecteur(candidatsEvaluesSurSecteur: List[CandidatDto],
                                               candidatsInteressesParAutreSecteur: List[CandidatDto]) extends ResultatRechercheCandidat {

  override val nbCandidats: Int = candidatsEvaluesSurSecteur.size + candidatsInteressesParAutreSecteur.size

}

case class ResultatRechercheCandidatParMetier(candidatsEvaluesSurMetier: List[CandidatDto],
                                              candidatsInteressesParMetierMemeSecteur: List[CandidatDto],
                                              candidatsInteressesParMetierAutreSecteur: List[CandidatDto]) extends ResultatRechercheCandidat {

  override val nbCandidats: Int = candidatsEvaluesSurMetier.size + candidatsInteressesParMetierMemeSecteur.size + candidatsInteressesParMetierAutreSecteur.size
}