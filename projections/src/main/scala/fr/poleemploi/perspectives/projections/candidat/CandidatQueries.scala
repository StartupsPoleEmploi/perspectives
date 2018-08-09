package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.Query
import fr.poleemploi.perspectives.domain.candidat.CandidatId
import fr.poleemploi.perspectives.domain.recruteur.{RecruteurId, TypeRecruteur}
import fr.poleemploi.perspectives.domain.{Metier, SecteurActivite}

sealed trait CandidatQuery extends Query

case class GetCandidatQuery(candidatId: CandidatId) extends CandidatQuery

case class GetCVParCandidatQuery(candidatId: CandidatId) extends CandidatQuery

case class GetCVPourRecruteurParCandidatQuery(candidatId: CandidatId,
                                              recruteurId: RecruteurId) extends CandidatQuery

case class RechercherCandidatsParDateInscriptionQuery(typeRecruteur: TypeRecruteur) extends CandidatQuery

case class RechercheCandidatsParSecteurQuery(typeRecruteur: TypeRecruteur,
                                             secteurActivite: SecteurActivite) extends CandidatQuery

case class RechercherCandidatsParMetierQuery(typeRecruteur: TypeRecruteur,
                                             metiers: Set[Metier]) extends CandidatQuery