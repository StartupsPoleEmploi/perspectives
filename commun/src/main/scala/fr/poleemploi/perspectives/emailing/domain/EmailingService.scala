package fr.poleemploi.perspectives.emailing.domain

import fr.poleemploi.perspectives.candidat.{Adresse, CandidatId}

import scala.concurrent.Future

trait EmailingService {

  def ajouterCandidatInscrit(candidatInscrit: CandidatInscrit): Future[Unit]

  def mettreAJourCVCandidat(candidatId: CandidatId, possedeCV: Boolean): Future[Unit]

  def mettreAJourAdresseCandidat(candidatId: CandidatId, adresse: Adresse): Future[Unit]

  def ajouterRecruteurInscrit(recruteurInscrit: RecruteurInscrit): Future[Unit]
}
