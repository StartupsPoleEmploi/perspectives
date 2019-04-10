package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.{CandidatId, LocalisationRecherche}
import fr.poleemploi.perspectives.projections.metier.MetierDTO

case class CandidatPourRechercheOffreQuery(candidatId: CandidatId) extends Query[CandidatPourRechercheOffreQueryResult]

case class CandidatPourRechercheOffreQueryResult(metiersValides: Set[MetierDTO],
                                                 localisationRecherche: Option[LocalisationRecherche],
                                                 cv: Boolean) extends QueryResult
