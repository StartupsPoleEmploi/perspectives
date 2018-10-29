package fr.poleemploi.perspectives.projections.candidat.mrs

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.Metier

case class MetiersEvaluesNouvelInscritQuery(candidatId: CandidatId) extends Query[MetiersEvaluesNouvelInscritQueryResult]

case class MetiersEvaluesNouvelInscritQueryResult(metiers: List[Metier]) extends QueryResult
