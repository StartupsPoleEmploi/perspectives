package fr.poleemploi.perspectives.offre.domain

import fr.poleemploi.perspectives.commun.domain.{CodeROME, RayonRecherche}

import scala.concurrent.Future

case class CriteresRechercheOffre(codesROME: List[CodeROME],
                                  codePostal: String,
                                  rayonRecherche: RayonRecherche) {

  val experience: Experience = Experience.DEBUTANT
}

trait ReferentielOffre {

  def rechercherOffres(criteres: CriteresRechercheOffre): Future[List[Offre]]

}
