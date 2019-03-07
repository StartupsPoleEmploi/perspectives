package fr.poleemploi.perspectives.offre.domain

import scala.concurrent.Future

case class RechercheOffreResult(offres : List[Offre],
                                nbOffresTotal: Int)

trait ReferentielOffre {

  def rechercherOffres(criteres: CriteresRechercheOffre): Future[RechercheOffreResult]

}
