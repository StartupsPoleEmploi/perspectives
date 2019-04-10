package fr.poleemploi.perspectives.emailing.domain

import scala.concurrent.Future

trait EmailingService {

  def ajouterCandidatInscrit(candidatInscrit: CandidatInscrit): Future[Unit]

  def mettreAJourCVCandidat(miseAJourCVCandidat: MiseAJourCVCandidat): Future[Unit]

  def ajouterRecruteurInscrit(recruteurInscrit: RecruteurInscrit): Future[Unit]
}
