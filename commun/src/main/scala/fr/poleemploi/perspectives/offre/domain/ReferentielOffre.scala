package fr.poleemploi.perspectives.offre.domain

import scala.concurrent.Future

trait ReferentielOffre {

  def rechercherOffres(criteres: CriteresRechercheOffre): Future[List[Offre]]

}
