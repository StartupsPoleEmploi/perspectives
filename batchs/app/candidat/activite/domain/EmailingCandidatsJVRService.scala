package candidat.activite.domain

import fr.poleemploi.perspectives.candidat.CandidatId

import scala.concurrent.Future

trait EmailingCandidatsJVRService {

  def envoyerEmailsCandidatsJVR: Future[Stream[CandidatId]]
}
