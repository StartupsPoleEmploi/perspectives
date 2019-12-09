package candidat.activite.domain

import fr.poleemploi.perspectives.emailing.domain.OffreGereeParConseillerAvecCandidats

import scala.concurrent.Future

trait ImportOffresEnDifficulteGereesParConseillerService {

  def importerOffresEnDifficulteGereesParConseiller: Future[Stream[OffreGereeParConseillerAvecCandidats]]
}
