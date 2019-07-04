package fr.poleemploi.perspectives.emailing.infra.mailjet

import fr.poleemploi.perspectives.candidat.{Adresse, CandidatId}
import fr.poleemploi.perspectives.emailing.domain._
import fr.poleemploi.perspectives.emailing.infra.sql.MailjetSqlAdapter
import fr.poleemploi.perspectives.emailing.infra.ws._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MailjetEmailingService(mailjetSqlAdapter: MailjetSqlAdapter,
                             mailjetWSAdapter: MailjetWSAdapter) extends EmailingService {

  override def ajouterCandidatInscrit(candidatInscrit: CandidatInscrit): Future[Unit] =
    for {
      mailjetContactId <- mailjetWSAdapter.ajouterCandidatInscrit(candidatInscrit)
      _ <- mailjetSqlAdapter.ajouterCandidat(CandidatMailjet(
        candidatId = candidatInscrit.candidatId,
        mailjetContactId = mailjetContactId,
        email = candidatInscrit.email
      ))
    } yield ()

  override def mettreAJourAdresseCandidat(candidatId: CandidatId, adresse: Adresse): Future[Unit] =
    for {
      candidatMailjet <- mailjetSqlAdapter.getCandidat(candidatId)
      _ <- mailjetWSAdapter.mettreAJourAdresseCandidat(candidatMailjet.mailjetContactId, adresse)
    } yield ()

  override def mettreAJourDerniereMRSValideeCandidat(candidatId: CandidatId, mrsValideeCandidat: MRSValideeCandidat): Future[Unit] =
    for {
      candidatMailjet <- mailjetSqlAdapter.getCandidat(candidatId)
      _ <- mailjetWSAdapter.mettreAJourDerniereMRSValideeCandidat(candidatMailjet.mailjetContactId, mrsValideeCandidat)
    } yield ()

  override def mettreAJourCVCandidat(candidatId: CandidatId, possedeCV: Boolean): Future[Unit] =
    for {
      candidatMailjet <- mailjetSqlAdapter.getCandidat(candidatId)
      _ <- mailjetWSAdapter.mettreAJourCVCandidat(candidatMailjet.mailjetContactId, possedeCV)
    } yield ()

  override def ajouterRecruteurInscrit(recruteurInscrit: RecruteurInscrit): Future[Unit] = {
    for {
      mailjetContactId <- mailjetWSAdapter.ajouterRecruteurInscrit(recruteurInscrit)
      _ <- mailjetSqlAdapter.ajouterRecruteur(
        RecruteurMailjet(
          recruteurId = recruteurInscrit.recruteurId,
          mailjetContactId = mailjetContactId,
          email = recruteurInscrit.email
        )
      )
    } yield ()
  }
}
