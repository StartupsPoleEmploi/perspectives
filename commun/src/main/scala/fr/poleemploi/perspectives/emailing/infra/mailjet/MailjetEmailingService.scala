package fr.poleemploi.perspectives.emailing.infra.mailjet

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

  override def mettreAJourCVCandidat(miseAJourCVCandidat: MiseAJourCVCandidat): Future[Unit] =
    for {
      candidatMailjet <- mailjetSqlAdapter.getCandidat(miseAJourCVCandidat.candidatId)
      _ <- mailjetWSAdapter.mettreAJourCandidat(candidatMailjet.email, miseAJourCVCandidat.possedeCV)
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

  override def envoyerAlerteMailRecruteur(alerteMailRecruteur: AlerteMailRecruteur): Future[Unit] =
    if (alerteMailRecruteur.nbCandidats > 0)
      mailjetWSAdapter.envoyerAlerteMailRecruteur(alerteMailRecruteur)
    else
      Future.successful(())
}
