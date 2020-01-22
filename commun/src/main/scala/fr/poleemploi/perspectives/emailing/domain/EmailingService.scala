package fr.poleemploi.perspectives.emailing.domain

import fr.poleemploi.perspectives.candidat.{Adresse, CandidatId}
import fr.poleemploi.perspectives.recruteur.{RecruteurId, TypeRecruteur}

import scala.concurrent.Future

trait EmailingService {

  def ajouterCandidatInscrit(candidatInscrit: CandidatInscrit): Future[Unit]

  def mettreAJourCVCandidat(candidatId: CandidatId, possedeCV: Boolean): Future[Unit]

  def mettreAJourAdresseCandidat(candidatId: CandidatId, adresse: Adresse): Future[Unit]

  def mettreAJourDerniereMRSValideeCandidat(candidatId: CandidatId, mrsValideeCandidat: MRSValideeCandidat): Future[Unit]

  def ajouterRecruteurInscrit(recruteurInscrit: RecruteurInscrit): Future[Unit]

  def mettreAJourTypeRecruteur(recruteurId: RecruteurId, typeRecruteur: TypeRecruteur): Future[Unit]

  def mettreAJourDisponibiliteCandidatJVR(candidatId: CandidatId, candidatEnRecherche: Boolean): Future[Unit]
}
