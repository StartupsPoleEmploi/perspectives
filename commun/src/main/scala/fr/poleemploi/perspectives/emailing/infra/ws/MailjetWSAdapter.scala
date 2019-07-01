package fr.poleemploi.perspectives.emailing.infra.ws

import java.nio.charset.StandardCharsets
import java.util.Base64

import fr.poleemploi.perspectives.candidat.Adresse
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

  def ajouterCandidatInscrit(candidatInscrit: CandidatInscrit): Future[MailjetContactId] =
    for {
      mailjetContactId <- updateContactData(
        email = candidatInscrit.email,
        request = mailjetWSMapping.buildContactRequestInscriptionCandidat(candidatInscrit)
      )
      _ <- manageContactLists(
        mailjetContactId = mailjetContactId,
        request = mailjetWSMapping.buildContactListsRequestInscriptionCandidat(candidatInscrit)
      )
    } yield mailjetContactId

  def mettreAJourCV(email: Email, possedeCV: Boolean): Future[Unit] =
    updateContactData(
      email = email,
      request = mailjetWSMapping.buildRequestMiseAJourCVCandidat(possedeCV)
    ).map(_ => ())

  def mettreAJourAdresse(email: Email, adresse: Adresse): Future[Unit] =
    updateContactData(
      email = email,
      request = mailjetWSMapping.buildRequestMiseAJourAdresseCandidat(adresse)
    ).map(_ => ())

  def ajouterRecruteurInscrit(recruteurInscrit: RecruteurInscrit): Future[MailjetContactId] =
    for {
      mailjetContactId <- updateContactData(
        email = recruteurInscrit.email,
        request = mailjetWSMapping.buildContactRequestInscriptionRecruteur(recruteurInscrit)
      )
      _ <- manageContactLists(
        mailjetContactId = mailjetContactId,
        request = mailjetWSMapping.buildContactListsRequestInscriptionRecruteur(recruteurInscrit)
      )
    } yield mailjetContactId

  private def sendTemplate(mailjetTemplateEmail: MailjetTemplateEmail): Future[Unit] =
    wsClient
      .url(s"${config.urlApi}/v3.1/send")
      .addHttpHeaders(jsonContentType, authorizationHeader)
      .post(Json.obj(
        "Messages" -> Json.toJson(mailjetTemplateEmail.messages)
      ))
      .flatMap(filtreStatutReponse(_))
      .map(_ => ())

  private def updateContactData(email: Email,
                                request: UpdateContactDataRequest): Future[MailjetContactId] =
    wsClient
      .url(s"${config.urlApi}/v3/REST/contactdata/${email.value}")
      .addHttpHeaders(jsonContentType, authorizationHeader)
      .put(Json.toJson(request))
      .flatMap(filtreStatutReponse(_))
      .map(_.json.as[UpdateContactDataResponse].contactId)

  private def manageContactLists(mailjetContactId: MailjetContactId,
                                 request: ManageContactListsRequest): Future[Unit] =
    wsClient
      .url(s"${config.urlApi}/v3/REST/contact/${mailjetContactId.value}/managecontactslists")
      .addHttpHeaders(jsonContentType, authorizationHeader)
      .post(Json.toJson(request))
      .flatMap(filtreStatutReponse(_))
      .map(_ => ())

  private def authorizationHeader: (String, String) = ("Authorization", s"Basic $authorization")
}
