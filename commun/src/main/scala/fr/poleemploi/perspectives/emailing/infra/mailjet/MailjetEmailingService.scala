package fr.poleemploi.perspectives.emailing.infra.mailjet

import fr.poleemploi.perspectives.emailing.domain.{CandidatInscrit, EmailingService, MiseAJourCVCandidat, RecruteurInscrit}
import fr.poleemploi.perspectives.emailing.infra.sql.MailjetSqlAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.{MailjetMapping, MailjetWSAdapter, ManageContactRequest}
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MailjetEmailingService(mailjetSqlAdapter: MailjetSqlAdapter,
                             mailjetWSAdapter: MailjetWSAdapter) extends EmailingService {

  override def ajouterCandidatInscrit(candidatInscrit: CandidatInscrit): Future[Unit] =
    for {
      manageContactResponse <- mailjetWSAdapter.ajouterCandidatInscrit(
        ManageContactRequest(
          email = candidatInscrit.email.value,
          name = Some(s"${candidatInscrit.nom.capitalize} ${candidatInscrit.prenom.capitalize}"),
          action = "addnoforce",
          properties = Json.obj(
            "nom" -> candidatInscrit.nom.capitalize,
            "prénom" -> candidatInscrit.prenom.capitalize, // doit comporter l'accent
            "genre" -> MailjetMapping.serializeGenre(candidatInscrit.genre),
            "cv" -> false
          )
        )
      )
      _ <- mailjetSqlAdapter.ajouterCandidat(CandidatMailjet(
        candidatId = candidatInscrit.candidatId,
        mailjetContactId = manageContactResponse.contactId,
        email = candidatInscrit.email
      ))
    } yield ()

  override def mettreAJourCVCandidat(miseAJourCVCandidat: MiseAJourCVCandidat): Future[Unit] =
    for {
      candidatMailjet <- mailjetSqlAdapter.getCandidat(miseAJourCVCandidat.candidatId)
      _ <- mailjetWSAdapter.mettreAJourCandidat(
        ManageContactRequest(
          email = candidatMailjet.email.value,
          action = "addnoforce",
          properties = Json.obj(
            "cv" -> miseAJourCVCandidat.possedeCV
          )
        )
      )
    } yield ()

  override def ajouterRecruteurInscrit(recruteurInscrit: RecruteurInscrit): Future[Unit] = {
    for {
      manageContactResponse <- mailjetWSAdapter.ajouterRecruteurInscrit(
        ManageContactRequest(
          email = recruteurInscrit.email.value,
          name = Some(s"${recruteurInscrit.nom.capitalize} ${recruteurInscrit.prenom.capitalize}"),
          action = "addnoforce",
          properties = Json.obj(
            "nom" -> recruteurInscrit.nom.capitalize,
            "prénom" -> recruteurInscrit.prenom.capitalize, // doit comporter l'accent
            "genre" -> MailjetMapping.serializeGenre(recruteurInscrit.genre)
          )
        )
      )
      _ <- mailjetSqlAdapter.ajouterRecruteur(
        RecruteurMailjet(
          recruteurId = recruteurInscrit.recruteurId,
          mailjetContactId = manageContactResponse.contactId,
          email = recruteurInscrit.email
        )
      )
    } yield ()
  }
}
