package fr.poleemploi.perspectives.offre.domain

import play.api.libs.json.{Json, Writes}

import scala.concurrent.Future

case class PageOffres(debut: Int,
                      fin: Int)

object PageOffres {

  implicit val writes: Writes[PageOffres] = Json.writes[PageOffres]
}

case class RechercheOffreResult(offres : List[Offre],
                                pageSuivante: Option[PageOffres])

trait ReferentielOffre {

  def rechercherOffres(criteres: CriteresRechercheOffre): Future[RechercheOffreResult]

}