package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{Metier, RayonRecherche}
import play.api.libs.json.{Json, Writes}

case class CandidatCriteresRechercheQuery(candidatId: CandidatId) extends Query[CandidatCriteresRechercheQueryResult]

case class CandidatCriteresRechercheQueryResult(candidatId: CandidatId,
                                                rechercheMetiersEvalues: Option[Boolean],
                                                metiersEvalues: List[Metier],
                                                rechercheAutresMetiers: Option[Boolean],
                                                metiersRecherches: List[Metier],
                                                codePostal: Option[String],
                                                commune: Option[String],
                                                rayonRecherche: Option[RayonRecherche]) extends QueryResult {

  def criteresComplet: Boolean =
    List(rechercheMetiersEvalues, rechercheAutresMetiers, codePostal, commune, rayonRecherche).forall(_.isDefined)
}

object CandidatCriteresRechercheQueryResult {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[CandidatCriteresRechercheQueryResult] = Json.writes[CandidatCriteresRechercheQueryResult]

}
