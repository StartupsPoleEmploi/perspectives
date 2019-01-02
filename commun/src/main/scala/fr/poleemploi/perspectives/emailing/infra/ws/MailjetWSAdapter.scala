package fr.poleemploi.perspectives.emailing.infra.ws

import java.nio.charset.StandardCharsets
import java.util.Base64

import fr.poleemploi.perspectives.commun.domain.Email
import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter
import fr.poleemploi.perspectives.emailing.domain._
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetContactId
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MailjetWSAdapter(config: MailjetWSAdapterConfig,
                       mailjetWSMapping: MailjetWSMapping,
                       wsClient: WSClient) extends WSAdapter {

  val authorization: String = Base64.getEncoder
    .encodeToString(s"${config.apiKeyPublic}:${config.apiKeyPrivate}".getBytes(StandardCharsets.UTF_8))

  val sender: String = config.senderAdress

  val idListeCandidatsInscrits: Int = 9908
  val idListeRecruteursInscrits: Int = 9909
  val idListeTesteurs: Int = 20603
  val alerteMailRecruteurTemplateId: Int = 570953

  def ajouterCandidatInscrit(candidatInscrit: CandidatInscrit): Future[MailjetContactId] =
    manageContact(
      idListeContact = idListeCandidatsInscrits,
      request = mailjetWSMapping.buildRequestCandidatInscrit(candidatInscrit)
    ).map(_.contactId)

  def mettreAJourCandidat(email: Email, possedeCV: Boolean): Future[Unit] =
    manageContact(
      idListeContact = idListeCandidatsInscrits,
      request = mailjetWSMapping.buildRequestMiseAJourCV(email, possedeCV)
    ).map(_ => ())

  def ajouterRecruteurInscrit(recruteurInscrit: RecruteurInscrit): Future[MailjetContactId] =
    manageContact(
      idListeContact = idListeRecruteursInscrits,
      request = mailjetWSMapping.buildRequestRecruteurInscrit(recruteurInscrit)
    ).map(_.contactId)

  def envoyerAlerteMailRecruteur(alerteMailRecruteur: AlerteMailRecruteur): Future[Unit] =
    sendTemplate(MailjetTemplateEmail(
      messages = List(MailjetTemplateMessage(
        from = MailjetSender(email = sender, name = ""),
        to = List(MailjetRecipient(email = alerteMailRecruteur.email.value, name = "")),
        subject = alerteMailRecruteur.sujet,
        templateID = alerteMailRecruteurTemplateId,
        templateLanguage = true,
        variables = Map(
          "texteInscription" -> alerteMailRecruteur.recapitulatifInscriptions,
          "lienConnexion" -> alerteMailRecruteur.lienConnexion
        )
      ))
    ))

  private def sendTemplate(mailjetTemplateEmail: MailjetTemplateEmail): Future[Unit] =
    wsClient
      .url(s"${config.urlApi}/v3.1/send")
      .addHttpHeaders(jsonHttpHeader, authorizationHeader)
      .post(Json.obj(
        "Messages" -> Json.toJson(mailjetTemplateEmail.messages)
      ))
      .flatMap(filtreStatutReponse(_))
      .map(_ => ())

  private def manageContact(idListeContact: Int, request: ManageContactRequest): Future[ManageContactResponse] =
    wsClient
      .url(s"${config.urlApi}/v3/REST/contactslist/${filtrerListeTesteurs(idListeContact, request.email)}/managecontact")
      .addHttpHeaders(jsonHttpHeader, authorizationHeader)
      .post(Json.toJson(request))
      .flatMap(filtreStatutReponse(_))
      .map(_.json.as[ManageContactResponse])

  private def jsonHttpHeader: (String, String) = ("Content-Type", "application/json")

  private def authorizationHeader: (String, String) = ("Authorization", s"Basic $authorization")

  private def filtrerListeTesteurs(idListe: Int, email: String): Int =
    if (config.testeurs.contains(email)) idListeTesteurs else idListe
}
