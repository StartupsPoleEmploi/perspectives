package fr.poleemploi.perspectives.projections.candidat

sealed trait ResultatRechercheCandidat {
  def nbCandidats: Int
}

case class ResultatRechercheCandidatParDateInscription(private val listeCandidats: ListeCandidats) extends ResultatRechercheCandidat {

  override val nbCandidats: Int = listeCandidats.nbCandidats

  val candidats: List[CandidatDto] = listeCandidats.candidatDtos
}

case class ResultatRechercheCandidatParSecteur(private val validesSecteur: ListeCandidats,
                                               private val interessesSecteur: ListeCandidats) extends ResultatRechercheCandidat {

  override val nbCandidats: Int = validesSecteur.nbCandidats + interessesSecteur.nbCandidats

  val nbCandidatsValidesSecteur: Int = validesSecteur.nbCandidats

  val candidatsValidesSecteur: List[CandidatDto] = validesSecteur.candidatDtos

  val nbCandidatsInteressesSecteur: Int = interessesSecteur.nbCandidats

  val candidatsInteressesSecteur: List[CandidatDto] = interessesSecteur.candidatDtos
}

case class ResultatRechercheCandidatParMetier(private val validesMetier: ListeCandidats,
                                              private val interessesMetier: ListeCandidats) extends ResultatRechercheCandidat {

  override val nbCandidats: Int = validesMetier.nbCandidats + interessesMetier.nbCandidats

  val nbCandidatsValidesMetier: Int = validesMetier.nbCandidats

  val candidatsValidesMetier: List[CandidatDto] = validesMetier.candidatDtos

  val nbCandidatsInteressesMetier: Int = interessesMetier.nbCandidats

  val candidatsInteressesMetier: List[CandidatDto] = interessesMetier.candidatDtos
}

/**
  * @param nbCandidats  Le nombre de candidats total correspondant à la recherche
  * @param candidatDtos La liste de candidats (la taille peut différer du total selon la pagination)
  */
case class ListeCandidats(nbCandidats: Int,
                          candidatDtos: List[CandidatDto])