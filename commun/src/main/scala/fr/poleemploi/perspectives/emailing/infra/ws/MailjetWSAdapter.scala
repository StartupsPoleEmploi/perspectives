package fr.poleemploi.perspectives.emailing.infra.ws

import java.nio.charset.StandardCharsets
import java.util.Base64

import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class MailjetEmailException(message: String) extends Exception(message)

class MailjetEmailAdapter(mailjetAdapterConfig: MailjetWSAdapterConfig,
                          wsClient: WSClient) {

  val authorization: String = Base64.getEncoder
    .encodeToString(s"${mailjetAdapterConfig.apiKeyPublic}:${mailjetAdapterConfig.apiKeyPrivate}".getBytes(StandardCharsets.UTF_8))

  val idListeCandidatsInscrits: Int = 9908
  val idListeRecruteursInscrits: Int = 9909
  val idListeTesteurs: Int = 20603

  val sender: String = mailjetAdapterConfig.senderAdress

  def sendTemplateEmail(mailjetTemplateEmail: MailjetTemplateEmail): Future[Unit] = {
    wsClient
      .url(s"${mailjetAdapterConfig.urlApi}/v3.1/send")
      .addHttpHeaders(jsonHttpHeader, authorizationHeader)
      .post(Json.obj(
        "Messages" -> Json.toJson(mailjetTemplateEmail.messages)
      ))
      .map(filtreStatutReponse(_))
  }

  def ajouterCandidatInscrit(request: ManageContactRequest): Future[ManageContactResponse] =
    manageContact(idListeCandidatsInscrits, request)

  def mettreAJourCandidat(request: ManageContactRequest): Future[ManageContactResponse] =
    manageContact(idListeCandidatsInscrits, request)

  def ajouterRecruteurInscrit(request: ManageContactRequest): Future[ManageContactResponse] =
    manageContact(idListeRecruteursInscrits, request)

  private def manageContact(idListeContact: Int, request: ManageContactRequest): Future[ManageContactResponse] =
    wsClient
      .url(s"${mailjetAdapterConfig.urlApi}/v3/REST/contactslist/${filtrerListeTesteurs(idListeContact, request.email)}/managecontact")
      .addHttpHeaders(jsonHttpHeader, authorizationHeader)
      .post(Json.toJson(request))
      .map(filtreStatutReponse(_))
      .map(_.json.as[ManageContactResponse])

  private def filtreStatutReponse(response: WSResponse,
                                  statutErreur: Int => Boolean = s => s >= 400,
                                  statutNonGere: Int => Boolean = s => s != 200 && s != 201): WSResponse = response.status match {
    case s if statutErreur(s) => throw MailjetEmailException(s"Erreur lors de l'envoi d'un email. Code: ${response.status}. Reponse : ${response.body}")
    case s if statutNonGere(s) => throw MailjetEmailException(s"Statut non géré lors de l'envoi d'un email. Code: ${response.status}. Reponse : ${response.body}")
    case _ => response
  }

  private def jsonHttpHeader: (String, String) = ("Content-Type", "application/json")

  private def authorizationHeader: (String, String) = ("Authorization", s"Basic $authorization")

  private def filtrerListeTesteurs(idListe: Int, email: String): Int =
    if (mailjetAdapterConfig.testeurs.contains(email)) idListeTesteurs else idListe
}
