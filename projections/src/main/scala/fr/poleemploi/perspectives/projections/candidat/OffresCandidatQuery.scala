package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.offre.domain._
import play.api.libs.json.{Json, Writes}

case class OffresCandidatQuery(criteresRechercheOffre: CriteresRechercheOffre) extends Query[OffresCandidatQueryResult]

case class OffresCandidatQueryResult(offres: List[Offre],
                                     pageSuivante: Option[PageOffres]) extends QueryResult

object OffresCandidatQueryResult {

  implicit val writes: Writes[OffresCandidatQueryResult] = Json.writes[OffresCandidatQueryResult]
}