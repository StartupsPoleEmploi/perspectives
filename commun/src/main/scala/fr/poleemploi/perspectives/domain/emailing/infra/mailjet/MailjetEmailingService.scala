package fr.poleemploi.perspectives.domain.emailing.infra.mailjet

import fr.poleemploi.perspectives.domain.emailing.{CandidatInscrit, EmailingService, MiseAJourCVCandidat, RecruteurInscrit}
import play.api.libs.json.{JsString, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MailjetEmailingService(mailjetContactAdapter: MailjetContactAdapter,
                             mailjetEmailAdapter: MailjetEmailAdapter) extends EmailingService {

  override def ajouterCandidatInscrit(candidatInscrit: CandidatInscrit): Future[Unit] =
    for {
      manageContactResponse <- mailjetEmailAdapter.ajouterCandidatInscrit(
        ManageContactRequest(
          email = candidatInscrit.email,
          name = Some(s"${candidatInscrit.nom.capitalize} ${candidatInscrit.prenom.capitalize}"),
          action = "addnoforce",
          properties = Json.obj(
            "nom" -> candidatInscrit.nom.capitalize,
            "prénom" -> candidatInscrit.prenom.capitalize, // doit comporter l'accent
            "genre" -> JsString(candidatInscrit.genre.map(MailjetMapping.serializeGenre).getOrElse("")),
            "cv" -> false
          )
        )
      )
      _ <- mailjetContactAdapter.saveCandidat(CandidatMailjet(
        candidatId = candidatInscrit.candidatId,
        mailjetContactId = manageContactResponse.contactId,
        email = candidatInscrit.email
      ))
    } yield ()

  override def mettreAJourCVCandidat(miseAJourCVCandidat: MiseAJourCVCandidat): Future[Unit] =
    for {
      candidatMailjet <- mailjetContactAdapter.getCandidat(miseAJourCVCandidat.candidatId)
      _ <- mailjetEmailAdapter.mettreAJourCandidat(
        ManageContactRequest(
          email = candidatMailjet.email,
          action = "addnoforce",
          properties = Json.obj(
            "cv" -> miseAJourCVCandidat.possedeCV
          )
        )
      )
    } yield ()

  override def ajouterRecruteurInscrit(recruteurInscrit: RecruteurInscrit): Future[Unit] = {
    for {
      manageContactResponse <- mailjetEmailAdapter.ajouterRecruteurInscrit(
        ManageContactRequest(
          email = recruteurInscrit.email,
          name = Some(s"${recruteurInscrit.nom.capitalize} ${recruteurInscrit.prenom.capitalize}"),
          action = "addnoforce",
          properties = Json.obj(
            "nom" -> recruteurInscrit.nom.capitalize,
            "prénom" -> recruteurInscrit.prenom.capitalize, // doit comporter l'accent
            "genre" -> MailjetMapping.serializeGenre(recruteurInscrit.genre)
          )
        )
      )
      _ <- mailjetContactAdapter.saveRecruteur(
        RecruteurMailjet(
          recruteurId = recruteurInscrit.recruteurId,
          mailjetContactId = manageContactResponse.contactId,
          email = recruteurInscrit.email
        )
      )
    } yield ()
  }
}
