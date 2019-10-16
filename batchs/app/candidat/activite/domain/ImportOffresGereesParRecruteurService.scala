package candidat.activite.domain

import fr.poleemploi.perspectives.emailing.domain.OffreGereeParRecruteurAvecCandidats

import scala.concurrent.Future

trait ImportOffresGereesParRecruteurService {

  def importerOffresGereesParRecruteur: Future[Stream[OffreGereeParRecruteurAvecCandidats]]
}
