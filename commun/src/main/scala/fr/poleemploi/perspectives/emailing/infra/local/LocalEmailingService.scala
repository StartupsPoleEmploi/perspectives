package fr.poleemploi.perspectives.emailing.infra.local

import fr.poleemploi.perspectives.emailing.domain.{CandidatInscrit, EmailingService, MiseAJourCVCandidat, RecruteurInscrit}

import scala.concurrent.Future

class LocalEmailingService extends EmailingService {

  override def ajouterCandidatInscrit(candidatInscrit: CandidatInscrit): Future[Unit] =
    Future.successful(())

  override def mettreAJourCVCandidat(miseAJourCVCandidat: MiseAJourCVCandidat): Future[Unit] =
    Future.successful(())

  override def ajouterRecruteurInscrit(recruteurInscrit: RecruteurInscrit): Future[Unit] =
    Future.successful(())
}
