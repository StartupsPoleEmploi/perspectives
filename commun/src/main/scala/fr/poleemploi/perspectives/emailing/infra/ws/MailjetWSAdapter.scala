package fr.poleemploi.perspectives.emailing.infra.ws

import java.nio.charset.StandardCharsets
import java.util.Base64

import fr.poleemploi.perspectives.candidat.Adresse
import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter
import fr.poleemploi.perspectives.emailing.domain._
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetContactId
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MailjetWSAdapter(config: MailjetWSAdapterConfig,
                       mapping: MailjetWSMapping,
                       wsClient: WSClient) extends WSAdapter {

  val authorization: String = Base64.getEncoder
    .encodeToString(s"${config.apiKeyPublic}:${config.apiKeyPrivate}".getBytes(StandardCharsets.UTF_8))

  def ajouterCandidatInscrit(candidatInscrit: CandidatInscrit): Future[MailjetContactId] = {
    val inscriptionRequest = mapping.buildRequestInscriptionCandidat(candidatInscrit)
    for {
      mailjetContactId <- manageContact(inscriptionRequest.idListe, inscriptionRequest.request)
      _ <- manageContactLists(mailjetContactId, mapping.buildContactListsRequestInscriptionCandidat)
    } yield mailjetContactId
  }

  def mettreAJourCVCandidat(mailjetContactId: MailjetContactId, possedeCV: Boolean): Future[Unit] =
    updateContactData(
      mailjetContactId = mailjetContactId,
      request = mapping.buildRequestMiseAJourCVCandidat(possedeCV)
    ).map(_ => ())

  def mettreAJourAdresseCandidat(mailjetContactId: MailjetContactId, adresse: Adresse): Future[Unit] =
    updateContactData(
      mailjetContactId = mailjetContactId,
      request = mapping.buildRequestMiseAJourAdresseCandidat(adresse)
    ).map(_ => ())

  def mettreAJourDerniereMRSValideeCandidat(mailjetContactId: MailjetContactId, mrsValideeCandidat: MRSValideeCandidat): Future[Unit] =
    updateContactData(
      mailjetContactId = mailjetContactId,
      request = mapping.buildRequestMiseAJourMRSValideeCandidat(mrsValideeCandidat)
    ).map(_ => ())

  def importerProspectsCandidats(prospects: Stream[MRSValideeProspectCandidat]): Future[Unit] =
    manageManyContacts(mapping.buildRequestImportProspectsCandidats(prospects))

  def ajouterRecruteurInscrit(recruteurInscrit: RecruteurInscrit): Future[MailjetContactId] = {
    val inscriptionRequest = mapping.buildRequestInscriptionRecruteur(recruteurInscrit)
    for {
      mailjetContactId <- manageContact(inscriptionRequest.idListe, inscriptionRequest.request)
      _ <- manageContactLists(mailjetContactId, mapping.buildContactListsRequestInscriptionRecruteur)
    } yield mailjetContactId
  }

  private def updateContactData(mailjetContactId: MailjetContactId,
                                request: UpdateContactDataRequest): Future[MailjetContactId] =
    wsClient
      .url(s"${config.urlApi}/v3/REST/contactdata/${mailjetContactId.value}")
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

  // Permet de créer ou de mettre à jour un contact en un seul appel en l'insérant dans une liste
  private def manageContact(idListe: Int, request: ManageContactRequest): Future[MailjetContactId] =
    wsClient
      .url(s"${config.urlApi}/v3/REST/contactslist/$idListe/managecontact")
      .addHttpHeaders(jsonContentType, authorizationHeader)
      .post(Json.toJson(request))
      .flatMap(filtreStatutReponse(_))
      .map(_.json.as[ManageContactResponse].mailjetContactId)

  private def manageManyContacts(request: ManageManyContactsRequest): Future[Unit] =
    wsClient
      .url(s"${config.urlApi}/v3/REST/contact/managemanycontacts")
      .addHttpHeaders(jsonContentType, authorizationHeader)
      .post(Json.toJson(request))
      .flatMap(filtreStatutReponse(_))
      .map(_ => ())

  private def authorizationHeader: (String, String) = ("Authorization", s"Basic $authorization")
}
