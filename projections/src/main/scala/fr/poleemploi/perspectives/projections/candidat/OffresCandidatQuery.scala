package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.offre.domain._

case class OffresCandidatQuery(criteresRechercheOffre: CriteresRechercheOffre) extends Query[OffresCandidatQueryResult]

case class OffresCandidatQueryResult(offres: List[Offre],
                                     nbOffresTotal: Int) extends QueryResult