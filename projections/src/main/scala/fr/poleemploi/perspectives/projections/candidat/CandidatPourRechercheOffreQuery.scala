package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{Metier, RayonRecherche}

case class CandidatPourRechercheOffreQuery(candidatId: CandidatId) extends Query[CandidatPourRechercheOffreQueryResult]

case class CandidatPourRechercheOffreQueryResult(candidatId: CandidatId,
                                                 metiersEvalues: List[Metier],
                                                 codePostal: Option[String],
                                                 commune: Option[String],
                                                 rayonRecherche: Option[RayonRecherche],
                                                 cv: Boolean) extends QueryResult
