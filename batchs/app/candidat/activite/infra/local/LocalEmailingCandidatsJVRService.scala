package candidat.activite.infra.local

import candidat.activite.domain.EmailingCandidatsJVRService
import fr.poleemploi.perspectives.candidat.CandidatId

import scala.concurrent.Future

class LocalEmailingCandidatsJVRService extends EmailingCandidatsJVRService {

  override def envoyerEmailsCandidatsJVR: Future[Stream[CandidatId]] =
    Future.successful(Stream.empty)
}
