package candidat.activite.domain

import fr.poleemploi.perspectives.candidat.activite.domain.EmailingDisponibiliteCandidatAvecEmail

import scala.concurrent.Future

trait EmailingDisponibilitesService {

  def envoyerEmailsDisponibilites: Future[Stream[EmailingDisponibiliteCandidatAvecEmail]]
}
