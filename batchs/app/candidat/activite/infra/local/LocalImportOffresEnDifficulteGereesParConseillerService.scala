package candidat.activite.infra.local

import candidat.activite.domain.ImportOffresEnDifficulteGereesParConseillerService
import fr.poleemploi.perspectives.emailing.domain.OffreGereeParConseillerAvecCandidats

import scala.concurrent.Future

class LocalImportOffresEnDifficulteGereesParConseillerService extends ImportOffresEnDifficulteGereesParConseillerService {

  override def importerOffresEnDifficulteGereesParConseiller: Future[Stream[OffreGereeParConseillerAvecCandidats]] =
    Future.successful(Stream.empty)
}
