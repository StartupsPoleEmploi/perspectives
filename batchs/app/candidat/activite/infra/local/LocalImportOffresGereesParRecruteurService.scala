package candidat.activite.infra.local

import candidat.activite.domain.ImportOffresGereesParRecruteurService
import fr.poleemploi.perspectives.emailing.domain.OffreGereeParRecruteurAvecCandidats

import scala.concurrent.Future

class LocalImportOffresGereesParRecruteurService extends ImportOffresGereesParRecruteurService {

  override def importerOffresGereesParRecruteur: Future[Stream[OffreGereeParRecruteurAvecCandidats]] =
    Future.successful(Stream.empty)
}
