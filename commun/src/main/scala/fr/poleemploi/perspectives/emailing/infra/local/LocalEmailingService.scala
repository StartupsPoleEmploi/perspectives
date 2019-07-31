package fr.poleemploi.perspectives.emailing.infra.local

import fr.poleemploi.perspectives.candidat.{Adresse, CandidatId}
import fr.poleemploi.perspectives.emailing.domain._
import fr.poleemploi.perspectives.recruteur.{RecruteurId, TypeRecruteur}

import scala.concurrent.Future

class LocalEmailingService extends EmailingService {

  override def ajouterCandidatInscrit(candidatInscrit: CandidatInscrit): Future[Unit] =
    Future.successful(())

  override def mettreAJourCVCandidat(candidatId: CandidatId, possedeCV: Boolean): Future[Unit] =
    Future.successful(())

  override def mettreAJourAdresseCandidat(candidatId: CandidatId, adresse: Adresse): Future[Unit] =
    Future.successful(())

  override def mettreAJourDerniereMRSValideeCandidat(candidatId: CandidatId, mrsValideeCandidat: MRSValideeCandidat): Future[Unit] =
    Future.successful(())

  override def ajouterRecruteurInscrit(recruteurInscrit: RecruteurInscrit): Future[Unit] =
    Future.successful(())

  override def mettreAJourTypeRecruteur(recruteurId: RecruteurId, typeRecruteur: TypeRecruteur): Future[Unit] =
    Future.successful(())
}
