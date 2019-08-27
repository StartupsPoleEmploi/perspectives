package candidat.activite.infra.local

import candidat.activite.domain.EmailingDisponibilitesService
import fr.poleemploi.perspectives.candidat.activite.domain.EmailingDisponibiliteCandidatAvecEmail

import scala.concurrent.Future

class LocalEmailingDisponibilitesService extends EmailingDisponibilitesService {

  override def envoyerEmailsDisponibilites: Future[Stream[EmailingDisponibiliteCandidatAvecEmail]] =
    Future.successful(Stream.empty)
}
