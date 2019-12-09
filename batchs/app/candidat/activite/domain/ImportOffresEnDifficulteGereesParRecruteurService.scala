package candidat.activite.domain

import fr.poleemploi.perspectives.emailing.domain.OffreGereeParRecruteurAvecCandidats

import scala.concurrent.Future

trait ImportOffresEnDifficulteGereesParRecruteurService {

  def importerOffresEnDifficulteGereesParRecruteur: Future[Stream[OffreGereeParRecruteurAvecCandidats]]
}
