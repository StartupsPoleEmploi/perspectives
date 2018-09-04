package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.CVId
import fr.poleemploi.perspectives.commun.domain._

case class RechercheCandidatDto(candidatId: CandidatId,
                                nom: String,
                                prenom: String,
                                email: Email,
                                commune: Option[String],
                                metiersEvalues: List[Metier],
                                habiletes: List[Habilete],
                                metiersRecherchesParSecteur: Map[SecteurActivite, List[Metier]],
                                rayonRecherche: Option[RayonRecherche],
                                numeroTelephone: Option[NumeroTelephone],
                                cvId: Option[CVId]) {

  def possedeCV: Boolean = cvId.isDefined
}

sealed trait ResultatRechercheCandidat {
  def nbCandidats: Int
}

case class ResultatRechercheCandidatParDateInscription(candidats: List[RechercheCandidatDto]) extends ResultatRechercheCandidat {

  override val nbCandidats: Int = candidats.size
}

case class ResultatRechercheCandidatParSecteur(candidatsEvaluesSurSecteur: List[RechercheCandidatDto],
                                               candidatsInteressesParAutreSecteur: List[RechercheCandidatDto]) extends ResultatRechercheCandidat {

  override val nbCandidats: Int = candidatsEvaluesSurSecteur.size + candidatsInteressesParAutreSecteur.size
}

case class ResultatRechercheCandidatParMetier(candidatsEvaluesSurMetier: List[RechercheCandidatDto],
                                              candidatsInteressesParMetierMemeSecteur: List[RechercheCandidatDto],
                                              candidatsInteressesParMetierAutreSecteur: List[RechercheCandidatDto]) extends ResultatRechercheCandidat {

  override val nbCandidats: Int = candidatsEvaluesSurMetier.size + candidatsInteressesParMetierMemeSecteur.size + candidatsInteressesParMetierAutreSecteur.size
}