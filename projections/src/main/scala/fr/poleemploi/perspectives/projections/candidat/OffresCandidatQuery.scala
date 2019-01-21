package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.offre.domain.{CriteresRechercheOffre, Offre}
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import play.api.libs.json.{Json, Writes}

case class OffresCandidatQuery(criteresRechercheOffre: CriteresRechercheOffre) extends Query[OffresCandidatQueryResult]

case class OffresCandidatQueryResult(offres: List[Offre]) extends QueryResult

object OffresCandidatQueryResult {

  implicit val writesOffre: Writes[Offre] = Json.writes[Offre]

}