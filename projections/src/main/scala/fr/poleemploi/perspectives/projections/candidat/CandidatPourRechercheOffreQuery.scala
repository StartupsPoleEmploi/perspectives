package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.{CandidatId, LocalisationRecherche}
import fr.poleemploi.perspectives.metier.domain.Metier

case class CandidatPourRechercheOffreQuery(candidatId: CandidatId) extends Query[CandidatPourRechercheOffreQueryResult]

case class CandidatPourRechercheOffreQueryResult(metiersValides: Set[Metier],
                                                 localisationRecherche: Option[LocalisationRecherche],
                                                 cv: Boolean) extends QueryResult
