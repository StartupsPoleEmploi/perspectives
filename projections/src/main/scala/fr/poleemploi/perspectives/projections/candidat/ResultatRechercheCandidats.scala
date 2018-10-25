package fr.poleemploi.perspectives.projections.candidat

sealed trait ResultatRechercheCandidat {

  def nbCandidats: Int
}

case class ResultatRechercheCandidatParDepartement(candidats: List[CandidatRechercheDto]) extends ResultatRechercheCandidat {

  override val nbCandidats: Int = candidats.size
}

case class ResultatRechercheCandidatParSecteur(candidatsEvaluesSurSecteur: List[CandidatRechercheDto],
                                               candidatsInteressesParAutreSecteur: List[CandidatRechercheDto]) extends ResultatRechercheCandidat {

  override val nbCandidats: Int = candidatsEvaluesSurSecteur.size + candidatsInteressesParAutreSecteur.size
}

case class ResultatRechercheCandidatParMetier(candidatsEvaluesSurMetier: List[CandidatRechercheDto],
                                              candidatsInteressesParMetierMemeSecteur: List[CandidatRechercheDto],
                                              candidatsInteressesParMetierAutreSecteur: List[CandidatRechercheDto]) extends ResultatRechercheCandidat {

  override val nbCandidats: Int = candidatsEvaluesSurMetier.size + candidatsInteressesParMetierMemeSecteur.size + candidatsInteressesParMetierAutreSecteur.size
}