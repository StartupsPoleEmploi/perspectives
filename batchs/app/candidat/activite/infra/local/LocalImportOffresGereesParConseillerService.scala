package candidat.activite.infra.local

import candidat.activite.domain.ImportOffresGereesParConseillerService
import fr.poleemploi.perspectives.emailing.domain.OffreGereeParConseillerAvecCandidats

import scala.concurrent.Future

class LocalImportOffresGereesParConseillerService extends ImportOffresGereesParConseillerService {

  override def importerOffresGereesParConseiller: Future[Stream[OffreGereeParConseillerAvecCandidats]] =
    Future.successful(Stream.empty)
}
