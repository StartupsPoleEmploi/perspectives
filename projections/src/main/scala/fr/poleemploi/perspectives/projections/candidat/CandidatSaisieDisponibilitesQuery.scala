package fr.poleemploi.perspectives.projections.candidat

import java.time.LocalDate

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.CandidatId

case class CandidatSaisieDisponibilitesQuery(candidatId: CandidatId) extends Query[CandidatSaisieDisponibilitesQueryResult]

case class CandidatSaisieDisponibilitesQueryResult(candidatId: CandidatId,
                                                   candidatEnRecherche: Boolean,
                                                   dateProchaineDisponibilite: Option[LocalDate],
                                                   emploiTrouveGracePerspectives: Boolean) extends QueryResult
