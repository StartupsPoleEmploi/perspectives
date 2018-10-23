package fr.poleemploi.perspectives.projections.candidat

sealed trait ResultatRechercheCandidat

case class ResultatRechercheCandidatParDepartement(candidats: List[CandidatRechercheDto]) extends ResultatRechercheCandidat

case class ResultatRechercheCandidatParSecteur(candidatsEvaluesSurSecteur: List[CandidatRechercheDto],
                                               candidatsInteressesParAutreSecteur: List[CandidatRechercheDto]) extends ResultatRechercheCandidat

case class ResultatRechercheCandidatParMetier(candidatsEvaluesSurMetier: List[CandidatRechercheDto],
                                              candidatsInteressesParMetierMemeSecteur: List[CandidatRechercheDto],
                                              candidatsInteressesParMetierAutreSecteur: List[CandidatRechercheDto]) extends ResultatRechercheCandidat