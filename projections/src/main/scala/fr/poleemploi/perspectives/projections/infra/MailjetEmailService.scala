package fr.poleemploi.perspectives.projections.infra

import java.nio.charset.StandardCharsets
import java.util.Base64

import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class MailjetEmailException(message: String) extends Exception(message)

class MailjetEmailService(mailjetConfig: MailjetConfig,
                          wsClient: WSClient) {

  val authorization: String = Base64.getEncoder
    .encodeToString(s"${mailjetConfig.apiKeyPublic}:${mailjetConfig.apiKeyPrivate}".getBytes(StandardCharsets.UTF_8))

  val idListeCandidatsInscrits: Int = 9908
  val idListeRecruteursInscrits: Int = 9909

  val sender: String = mailjetConfig.senderAdress

  def sendEmail(mailjetEmail: MailjetEmail): Future[Unit] = {
    wsClient
      .url(s"${mailjetConfig.urlApi}/v3.1/send")
      .addHttpHeaders(jsonHttpHeader, authorizationHeader)
      .post(Json.obj(
        "Messages" -> Json.toJson(mailjetEmail.messages)
      ))
      .map(filtreStatutReponse(_))
  }

  def sendTemplateEmail(mailjetTemplateEmail: MailjetTemplateEmail): Future[Unit] = {
    wsClient
      .url(s"${mailjetConfig.urlApi}/v3.1/send")
      .addHttpHeaders(jsonHttpHeader, authorizationHeader)
      .post(Json.obj(
        "Messages" -> Json.toJson(mailjetTemplateEmail.messages)
      ))
      .map(filtreStatutReponse(_))
  }

  def addCandidatInscrit(request: AddContactRequest): Future[Unit] =
    wsClient
      .url(s"${mailjetConfig.urlApi}/v3/REST/contactslist/$idListeCandidatsInscrits/managecontact")
      .addHttpHeaders(jsonHttpHeader, authorizationHeader)
      .post(Json.toJson(request))
      .map(filtreStatutReponse(_))

  def addRecruteurInscrit(request: AddContactRequest): Future[Unit] =
    wsClient
      .url(s"${mailjetConfig.urlApi}/v3/REST/contactslist/$idListeRecruteursInscrits/managecontact")
      .addHttpHeaders(jsonHttpHeader, authorizationHeader)
      .post(Json.toJson(request))
      .map(filtreStatutReponse(_))

  private def filtreStatutReponse(response: WSResponse,
                                  statutErreur: Int => Boolean = s => s >= 400,
                                  statutNonGere: Int => Boolean = s => s != 200 && s != 201): WSResponse = response.status match {
    case s if statutErreur(s) => throw MailjetEmailException(s"Erreur lors de l'envoi d'un email. Code: ${response.status}. Reponse : ${response.body}")
    case s if statutNonGere(s) => throw MailjetEmailException(s"Statut non géré lors de l'envoi d'un email. Code: ${response.status}. Reponse : ${response.body}")
    case _ => response
  }

  private def jsonHttpHeader: (String, String) = ("Content-Type", "application/json")

  private def authorizationHeader: (String, String) = ("Authorization", s"Basic $authorization")
}
