package candidat.activite.domain

import fr.poleemploi.perspectives.emailing.domain.OffreGereeParConseillerAvecCandidats

import scala.concurrent.Future

trait ImportOffresGereesParConseillerService {

  def importerOffresGereesParConseiller: Future[Stream[OffreGereeParConseillerAvecCandidats]]
}
