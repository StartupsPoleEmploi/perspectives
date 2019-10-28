package fr.poleemploi.perspectives.emailing.infra.ws

import java.nio.charset.StandardCharsets
import java.util.Base64

import fr.poleemploi.perspectives.candidat.Adresse
import fr.poleemploi.perspectives.candidat.activite.domain.EmailingDisponibiliteCandidatAvecEmail
import fr.poleemploi.perspectives.commun.domain.Email
import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter
import fr.poleemploi.perspectives.emailing.domain._
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetContactId
import fr.poleemploi.perspectives.recruteur.TypeRecruteur
import play.api.cache.AsyncCacheApi
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class MailjetWSAdapter(config: MailjetWSAdapterConfig,
                       mapping: MailjetWSMapping,
                       wsClient: WSClient,
                       cacheApi: AsyncCacheApi) extends WSAdapter {

  private val idListeCandidatsInscrits: Int = 9908
  private val idListeCandidatsProspects: Int = 10066519

  private val idListeRecruteursInscrits: Int = 9909
  private val idListeRecruteursProspects: Int = 10145943

  private val idListeTesteurs: Int = 10145941

  private val idTemplateDisponibiliteCandidat: Int = 1001166

  private val idTemplateOffreGereeParRecruteur: Int = 1043344

  private val idTemplateOffreGereeParConseiller: Int = 1042288

  private val cacheKeyTesteurs = "mailjetWSAdapter.testeurs"

  private val authorization: String = Base64.getEncoder
    .encodeToString(s"${config.apiKeyPublic}:${config.apiKeyPrivate}".getBytes(StandardCharsets.UTF_8))

  // limitation de l'API /send de mailjet
  private val nbMaxDestinataires: Int = 50

  def ajouterCandidatInscrit(candidatInscrit: CandidatInscrit): Future[MailjetContactId] =
    for {
      idListe <- filtrerTesteur(idListeCandidatsInscrits, candidatInscrit.email)
      mailjetContactId <- manageContact(idListe, mapping.buildContactRequestInscriptionCandidat(candidatInscrit))
      _ <- manageContactLists(mailjetContactId, mapping.buildSuppressionContactListRequest(idListeCandidatsProspects))
    } yield mailjetContactId

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

  def envoyerDisponibilitesCandidat(baseUrl: String, candidats: Seq[EmailingDisponibiliteCandidatAvecEmail]): Future[Unit] =
    if (candidats.nonEmpty)
      Future.sequence(candidats.grouped(nbMaxDestinataires).map(candidatChunk =>
        sendMail(mapping.buildRequestEmailDisponibiliteCandidat(
          baseUrl = baseUrl,
          candidats = candidatChunk,
          idTemplate = idTemplateDisponibiliteCandidat
        ))
      )).map(_ => ())
    else
      Future.successful(())

  def envoyerCandidatsPourOffreGereeParRecruteur(baseUrl: String, offresGereesParRecruteurAvecCandidats: Seq[OffreGereeParRecruteurAvecCandidats]): Future[Unit] =
    if (offresGereesParRecruteurAvecCandidats.nonEmpty)
      Future.sequence(offresGereesParRecruteurAvecCandidats.grouped(nbMaxDestinataires).map(offresChunk =>
        sendMail(mapping.buildRequestCandidatsPourOffreGereeParRecruteur(
          baseUrl = baseUrl,
          offresGereesParRecruteurAvecCandidats = offresChunk,
          idTemplate = idTemplateOffreGereeParRecruteur
        ))
      )).map(_ => ())
    else
      Future.successful(())

  def envoyerCandidatsPourOffreGereeParConseiller(baseUrl: String, offresGereesParConseillerAvecCandidats: Seq[OffreGereeParConseillerAvecCandidats]): Future[Unit] =
    if (offresGereesParConseillerAvecCandidats.nonEmpty)
      Future.sequence(offresGereesParConseillerAvecCandidats.grouped(nbMaxDestinataires).map(offresChunk =>
        sendMail(mapping.buildRequestCandidatsPourOffreGereeParConseiller(
          baseUrl = baseUrl,
          offresGereesParConseillerAvecCandidats = offresChunk,
          idTemplate = idTemplateOffreGereeParConseiller
        ))
      )).map(_ => ())
    else
      Future.successful(())

  def importerProspectsCandidats(prospects: Stream[MRSProspectCandidat]): Future[Unit] =
    if (prospects.nonEmpty)
      manageManyContacts(mapping.buildRequestImportProspectsCandidats(idListeCandidatsProspects, prospects))
    else
      Future.successful(())

  def ajouterRecruteurInscrit(recruteurInscrit: RecruteurInscrit): Future[MailjetContactId] =
    for {
      idListe <- filtrerTesteur(idListeRecruteursInscrits, recruteurInscrit.email)
      mailjetContactId <- manageContact(idListe, mapping.buildContactRequestInscriptionRecruteur(recruteurInscrit))
      _ <- manageContactLists(mailjetContactId, mapping.buildSuppressionContactListRequest(idListeRecruteursProspects))
    } yield mailjetContactId

  def mettreAJourTypeRecruteur(mailjetContactId: MailjetContactId, typeRecruteur: TypeRecruteur): Future[Unit] =
    updateContactData(
      mailjetContactId = mailjetContactId,
      request = mapping.buildRequestMiseAJourTypeRecruteur(typeRecruteur)
    ).map(_ => ())

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

  private def filtrerTesteur(idListe: Int, email: Email): Future[Int] =
    cacheApi.getOrElseUpdate(key = cacheKeyTesteurs, expiration = 1.day)(
      wsClient
        .url(s"${config.urlApi}/v3/REST/contact?ContactsList=$idListeTesteurs&Limit=100")
        .addHttpHeaders(jsonContentType, authorizationHeader)
        .get()
        .flatMap(filtreStatutReponse(_))
        .map(r => (r.json \ "Data" \\ "Email").map(_.as[String]).map(Email(_)).toList)
    ).map(t => if (t.contains(email)) idListeTesteurs else idListe)

  private def sendMail(request: SendMailRequest): Future[Unit] =
    wsClient
      .url(s"${config.urlApi}/v3.1/send")
      .addHttpHeaders(jsonContentType, authorizationHeader)
      .post(Json.toJson(request))
      .flatMap(filtreStatutReponse(_))
      .map(_ => ())

  private def authorizationHeader: (String, String) = ("Authorization", s"Basic $authorization")
}
