package candidat.activite.infra.local

import candidat.activite.domain.ImportOffresEnDifficulteGereesParRecruteurService
import fr.poleemploi.perspectives.emailing.domain.OffreGereeParRecruteurAvecCandidats

import scala.concurrent.Future

class LocalImportOffresEnDifficulteGereesParRecruteurService extends ImportOffresEnDifficulteGereesParRecruteurService {

  def importerOffresEnDifficulteGereesParRecruteur: Future[Stream[OffreGereeParRecruteurAvecCandidats]] =
    Future.successful(Stream.empty)
}
