package fr.poleemploi.perspectives.emailing.infra.ws

import java.time.format.DateTimeFormatter

import fr.poleemploi.perspectives.candidat.Adresse
import fr.poleemploi.perspectives.candidat.activite.domain.EmailingDisponibiliteCandidatAvecEmail
import fr.poleemploi.perspectives.commun.domain.Genre
import fr.poleemploi.perspectives.commun.infra.email.TrackingUtils
import fr.poleemploi.perspectives.emailing.domain._
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetContactId
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSMapping._
import fr.poleemploi.perspectives.recruteur.TypeRecruteur
import play.api.libs.functional.syntax._
import play.api.libs.json._

class MailjetWSMapping {

  import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetContactProperties._

  val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
  val prettyDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  def buildContactRequestInscriptionCandidat(candidatInscrit: CandidatInscrit): ManageContactRequest =
    ManageContactRequest(
      email = candidatInscrit.email.value,
      name = Some(s"${candidatInscrit.nom.value} ${candidatInscrit.prenom.value}"),
      action = "addnoforce",
      properties = Json.obj(
        nom -> candidatInscrit.nom.value,
        prenom -> candidatInscrit.prenom.value,
        genre -> buildGenre(candidatInscrit.genre),
        cv -> false
      )
    )

  def buildSuppressionContactListRequest(idListe: Int): ManageContactListsRequest =
    ManageContactListsRequest(List(
      ContactList(listID = s"$idListe", action = "remove")
    ))

  def buildRequestMiseAJourTypeRecruteur(typeRecruteur: TypeRecruteur): UpdateContactDataRequest =
    UpdateContactDataRequest(List(
      ContactDataProperty(type_recruteur, buildTypeRecruteur(typeRecruteur))
    ))

  def buildContactRequestInscriptionRecruteur(recruteurInscrit: RecruteurInscrit): ManageContactRequest =
    ManageContactRequest(
      email = recruteurInscrit.email.value,
      name = Some(s"${recruteurInscrit.nom.value} ${recruteurInscrit.prenom.value}"),
      action = "addnoforce",
      properties = Json.obj(
        nom -> recruteurInscrit.nom.value,
        prenom -> recruteurInscrit.prenom.value,
        genre -> buildGenre(recruteurInscrit.genre)
      )
    )

  def buildRequestMiseAJourCVCandidat(possedeCV: Boolean): UpdateContactDataRequest =
    UpdateContactDataRequest(List(
      ContactDataProperty(cv, String.valueOf(possedeCV))
    ))

  def buildRequestMiseAJourAdresseCandidat(adresse: Adresse): UpdateContactDataRequest =
    UpdateContactDataRequest(List(
      ContactDataProperty(departement, adresse.codePostal.take(2))
    ))

  def buildRequestMiseAJourMRSValideeCandidat(mrsValideeCandidat: MRSValideeCandidat): UpdateContactDataRequest =
    UpdateContactDataRequest(List(
      ContactDataProperty(mrs_code_rome, mrsValideeCandidat.metier.codeROME.value),
      ContactDataProperty(mrs_metier, mrsValideeCandidat.metier.label),
      ContactDataProperty(mrs_date, mrsValideeCandidat.dateEvaluation.atStartOfDay().format(formatter))
    ))

  def buildRequestImportProspectsCandidats(idListe: Int, prospectsCandidats: Stream[MRSProspectCandidat]): ManageManyContactsRequest =
    ManageManyContactsRequest(
      contacts = prospectsCandidats.map(p => Contact(
        email = p.email.value,
        properties = Json.obj(
          nom -> p.nom.value,
          prenom -> p.prenom.value,
          genre -> buildGenre(p.genre),
          departement -> p.codeDepartement.value,
          mrs_metier -> p.metier.label,
          mrs_code_rome -> p.metier.codeROME.value,
          mrs_date -> s"${p.dateEvaluation.atStartOfDay().format(formatter)}",
          id_peconnect -> p.peConnectId.value,
          identifiant_local -> p.identifiantLocal.value,
          code_neptune -> p.codeNeptune.value
        )
      )).toList,
      contactsLists = List(ContactList(
        listID = s"$idListe",
        action = "addnoforce"
      ))
    )

  def buildRequestEmailDisponibiliteCandidat(baseUrl: String, idTemplate: Int, candidats: Seq[EmailingDisponibiliteCandidatAvecEmail]): SendMailRequest =
    SendMailRequest(
      messages = candidats.map(c => SendMailMessage(
        from = None,
        to = Seq(EmailAndName(email = c.email.value)),
        subject = None,
        templateId = idTemplate,
        category = Some(DISPONIBILITE_CANDIDAT_CATEGORY),
        variables = Map(
          VAR_URL_FORMULAIRE_DISPO_CANDIDAT_EN_RECHERCHE -> s"$baseUrl/candidat/disponibilites?candidatEnRecherche=true&token=${c.autologinToken.value}",
          VAR_URL_FORMULAIRE_DISPO_CANDIDAT_PAS_EN_RECHERCHE -> s"$baseUrl/candidat/disponibilites?candidatEnRecherche=false&token=${c.autologinToken.value}"
        )
      ))
    )

  def buildRequestCandidatsPourOffreGereeParRecruteur(baseUrl: String,
                                                      idTemplate: Int,
                                                      offresGereesParRecruteurAvecCandidats: Seq[OffreGereeParRecruteurAvecCandidats]): SendMailRequest = {
    val utmCampaign = "offre-sans-preselection"
    SendMailRequest(
      messages = offresGereesParRecruteurAvecCandidats.map(offre => SendMailMessage(
        from = None,
        to = Seq(EmailAndName(email = offre.emailCorrespondant.value)),
        subject = None,
        templateId = idTemplate,
        category = Some(OFFRE_GEREE_PAR_RECRUTEUR_CATEGORY),
        variables = Map(
          VAR_TITRE_POSTE -> offre.intitule,
          VAR_OFFRE_ID -> offre.offreId.value,
          VAR_URL_RECHERCHE_CANDIDATS -> buildUrlRechercheCandidats(baseUrl, offre, utmCampaign),
          VAR_URL_PERSPECTIVES -> buildUrlHome(baseUrl, offre, "header", utmCampaign)
        )
      ))
    )
  }

  def buildRequestCandidatsPourOffreEnDifficulteGereeParRecruteur(baseUrl: String,
                                                                  idTemplate: Int,
                                                                  useVersionA: Boolean,
                                                                  offresGereesParRecruteurAvecCandidats: Seq[OffreGereeParRecruteurAvecCandidats]): SendMailRequest = {
    val utmCampaign = "offre-en-difficulte-sans-preselection" + (if(useVersionA) "-version-a" else "-version-b")
    SendMailRequest(
      messages = offresGereesParRecruteurAvecCandidats.map(offre => SendMailMessage(
        from = None,
        to = Seq(EmailAndName(email = offre.emailCorrespondant.value)),
        subject = None,
        templateId = idTemplate,
        category = Some(OFFRE_EN_DIFFICULTE_GEREE_PAR_RECRUTEUR_CATEGORY + (if(useVersionA) "_version_a" else "_version_b")),
        variables = Map(
          VAR_TITRE_POSTE -> offre.intitule,
          VAR_OFFRE_ID -> offre.offreId.value,
          VAR_DATE_OFFRE -> offre.datePublication.format(prettyDateFormatter),
          VAR_URL_RECHERCHE_CANDIDATS -> buildUrlRechercheCandidats(baseUrl, offre, utmCampaign),
          VAR_URL_PERSPECTIVES -> buildUrlHome(baseUrl, offre, "header", utmCampaign)
        )
      ))
    )
  }

  def buildRequestCandidatsPourOffreGereeParConseiller(baseUrl: String,
                                                       idTemplate: Int,
                                                       offresGereesParConseillerAvecCandidats: Seq[OffreGereeParConseillerAvecCandidats]): SendMailRequest = {
    val utmCampaign = "offre-avec-preselection"
    SendMailRequest(
      messages = offresGereesParConseillerAvecCandidats.map(offre => SendMailMessage(
        from = None,
        to = Seq(EmailAndName(email = offre.emailCorrespondant.value)),
        subject = None,
        templateId = idTemplate,
        category = Some(OFFRE_GEREE_PAR_CONSEILLER_CATEGORY),
        variables = Map(
          VAR_TITRE_POSTE -> offre.intitule,
          VAR_OFFRE_ID -> offre.offreId.value,
          VAR_URL_RECHERCHE_CANDIDATS -> buildUrlRechercheCandidats(baseUrl, offre, utmCampaign),
          VAR_URL_PERSPECTIVES -> buildUrlHome(baseUrl, offre, "header", utmCampaign),
          VAR_URL_PERSPECTIVES_2 -> buildUrlHome(baseUrl, offre, "cta-2", utmCampaign)
        )
      ))
    )
  }

  def buildRequestCandidatsPourOffreEnDifficulteGereeParConseiller(baseUrl: String,
                                                                   idTemplate: Int,
                                                                   offresGereesParConseillerAvecCandidats: Seq[OffreGereeParConseillerAvecCandidats]): SendMailRequest = {
    val utmCampaign = "offre-en-difficulte-avec-preselection"
    SendMailRequest(
      messages = offresGereesParConseillerAvecCandidats.map(offre => SendMailMessage(
        from = None,
        to = Seq(EmailAndName(email = offre.emailCorrespondant.value)),
        subject = None,
        templateId = idTemplate,
        category = Some(OFFRE_EN_DIFFICULTE_GEREE_PAR_CONSEILLER_CATEGORY),
        variables = Map(
          VAR_TITRE_POSTE -> offre.intitule,
          VAR_OFFRE_ID -> offre.offreId.value,
          VAR_DATE_OFFRE -> offre.datePublication.format(prettyDateFormatter),
          VAR_URL_RECHERCHE_CANDIDATS -> buildUrlRechercheCandidats(baseUrl, offre, utmCampaign),
          VAR_URL_PERSPECTIVES -> buildUrlHome(baseUrl, offre, "header", utmCampaign),
          VAR_URL_PERSPECTIVES_2 -> buildUrlHome(baseUrl, offre, "cta-2", utmCampaign)
        )
      ))
    )
  }

  private def buildUrlRechercheCandidats(baseUrl: String,
                                         offre: OffreAvecCandidats,
                                         utmCampaign: String): String = {
    val gaTracking = TrackingUtils.buildTrackingGA(
      utmCampaign = utmCampaign,
      utmSource = s"offre-${offre.offreId.value}",
      utmMedium = "email",
      utmContent = "cta"
    )
    s"$baseUrl/recruteur/recherche?codeRome=${offre.codeROME.value}&latitude=${offre.coordonnees.latitude}&longitude=${offre.coordonnees.longitude}&localisation=${offre.lieuTravail}&$gaTracking"
  }

  private def buildUrlHome(baseUrl: String,
                           offre: OffreAvecCandidats,
                           content: String,
                           utmCampaign: String): String = {
    val gaTracking = TrackingUtils.buildTrackingGA(
      utmCampaign = utmCampaign,
      utmSource = s"offre-${offre.offreId.value}",
      utmMedium = "email",
      utmContent = content
    )
    s"$baseUrl?$gaTracking"
  }

  private def buildGenre(genre: Genre): String = genre match {
    case Genre.HOMME => "M."
    case Genre.FEMME => "Mme"
    case g@_ => throw new IllegalArgumentException(s"Genre inconnu : $g")
  }

  private def buildTypeRecruteur(typeRecruteur: TypeRecruteur): String = typeRecruteur match {
    case TypeRecruteur.ENTREPRISE => "Entreprise"
    case TypeRecruteur.AGENCE_INTERIM => "Agence d'intérim"
    case TypeRecruteur.ORGANISME_FORMATION => "Organisme de formation"
    case t@_ => throw new IllegalArgumentException(s"TypeRecruteur non géré : ${t.value}")
  }
}

object MailjetWSMapping {
  val VAR_URL_FORMULAIRE_DISPO_CANDIDAT_EN_RECHERCHE = "urlFormulaireDispoOui"
  val VAR_URL_FORMULAIRE_DISPO_CANDIDAT_PAS_EN_RECHERCHE = "urlFormulaireDispoNon"
  val DISPONIBILITE_CANDIDAT_CATEGORY = "disponibilite_candidat"
  val VAR_URL_PERSPECTIVES = "urlPerspectives"
  val VAR_URL_PERSPECTIVES_2 = "urlPerspectives2"
  val VAR_URL_RECHERCHE_CANDIDATS = "urlRechercheCandidats"
  val VAR_OFFRE_ID = "offreId"
  val VAR_TITRE_POSTE = "titrePoste"
  val VAR_DATE_OFFRE = "dateOffre"
  val OFFRE_GEREE_PAR_RECRUTEUR_CATEGORY = "offre_geree_par_recruteur"
  val OFFRE_EN_DIFFICULTE_GEREE_PAR_RECRUTEUR_CATEGORY = "offre_en_difficulte_geree_par_recruteur"
  val OFFRE_GEREE_PAR_CONSEILLER_CATEGORY = "offre_geree_par_conseiller"
  val OFFRE_EN_DIFFICULTE_GEREE_PAR_CONSEILLER_CATEGORY = "offre_en_difficulte_geree_par_conseiller"
}

case class ContactList(listID: String,
                       action: String)

object ContactList {

  implicit val writes: Writes[ContactList] = (
    (JsPath \ "ListID").write[String] and
      (JsPath \ "Action").write[String]
    ) (unlift(ContactList.unapply))
}

case class ManageContactListsRequest(contactsList: List[ContactList])

object ManageContactListsRequest {

  implicit val writes: Writes[ManageContactListsRequest] =
    (__ \ "ContactsLists").write[List[ContactList]].contramap(_.contactsList)
}

case class Contact(email: String,
                   name: Option[String] = None,
                   properties: JsObject)

object Contact {

  implicit val writes: Writes[Contact] = (
    (JsPath \ "Email").write[String] and
      (JsPath \ "Name").writeNullable[String] and
      (JsPath \ "Properties").write[JsObject]
    ) (unlift(Contact.unapply))
}

case class ManageManyContactsRequest(contacts: List[Contact],
                                     contactsLists: List[ContactList])

object ManageManyContactsRequest {

  implicit val writes: Writes[ManageManyContactsRequest] = (
    (JsPath \ "Contacts").write[List[Contact]] and
      (JsPath \ "ContactsLists").write[List[ContactList]]
    ) (unlift(ManageManyContactsRequest.unapply))
}

case class ContactDataProperty(name: String,
                               value: String)

object ContactDataProperty {

  implicit val writes: Writes[ContactDataProperty] = (
    (JsPath \ "Name").write[String] and
      (JsPath \ "Value").write[String]
    ) (unlift(ContactDataProperty.unapply))
}

case class UpdateContactDataRequest(properties: List[ContactDataProperty])

object UpdateContactDataRequest {

  implicit val writes: Writes[UpdateContactDataRequest] =
    (__ \ "Data").write[List[ContactDataProperty]].contramap(_.properties)
}

case class UpdateContactDataResponse(count: Int,
                                     contactId: MailjetContactId)

object UpdateContactDataResponse {

  implicit val reads: Reads[UpdateContactDataResponse] = (
    (JsPath \ "Count").read[Int] and
      (JsPath \ "Data" \\ "ContactID").read[Long].map(MailjetContactId)
    ) (UpdateContactDataResponse.apply _)
}

case class ManageContactRequest(email: String,
                                name: Option[String] = None,
                                action: String,
                                properties: JsObject)

object ManageContactRequest {

  implicit val manageContactRequestWrites: Writes[ManageContactRequest] = (
    (JsPath \ "Email").write[String] and
      (JsPath \ "Name").write[Option[String]] and
      (JsPath \ "Action").write[String] and
      (JsPath \ "Properties").write[JsObject]
    ) (unlift(ManageContactRequest.unapply))
}

case class ManageContactResponse(count: Int,
                                 mailjetContactId: MailjetContactId)

object ManageContactResponse {

  implicit val manageContactReponseReads: Reads[ManageContactResponse] = (
    (JsPath \ "Count").read[Int] and
      (JsPath \ "Data" \\ "ContactID").read[Long].map(MailjetContactId)
    ) (ManageContactResponse.apply _)
}

case class ContactPropertiesResponse(count: Int,
                                     properties: List[ContactPropertyResponse])

object ContactPropertiesResponse {

  implicit val reads: Reads[ContactPropertiesResponse] = (
    (JsPath \ "Count").read[Int] and
      (JsPath \ "Data" \\ "Data").read[List[ContactPropertyResponse]]
    ) (ContactPropertiesResponse.apply _)
}

case class ContactPropertyResponse(name: String,
                                   value: String)

object ContactPropertyResponse {

  implicit val reads: Reads[ContactPropertyResponse] = (
    (JsPath \ "Name").read[String] and
      (JsPath \ "Value").read[String]
    ) (ContactPropertyResponse.apply _)
}

case class EmailAndName(email: String,
                        name: Option[String] = None)

object EmailAndName {
  implicit val writes: Writes[EmailAndName] = (
    (JsPath \ "Email").write[String] and
      (JsPath \ "Name").writeNullable[String]
    ) (unlift(EmailAndName.unapply))
}

case class SendMailMessage(from: Option[EmailAndName],
                           to: Seq[EmailAndName],
                           subject: Option[String],
                           templateId: Int,
                           templateLanguage: Boolean = true,
                           category: Option[String] = None,
                           variables: Map[String, String] = Map())

object SendMailMessage {
  implicit val writes: Writes[SendMailMessage] = (
    (JsPath \ "From").writeNullable[EmailAndName] and
      (JsPath \ "To").write[Seq[EmailAndName]] and
      (JsPath \ "Subject").writeNullable[String] and
      (JsPath \ "TemplateID").write[Int] and
      (JsPath \ "TemplateLanguage").write[Boolean] and
      (JsPath \ "CustomCampaign").writeNullable[String] and
      (JsPath \ "Variables").write[Map[String, String]]
    ) (unlift(SendMailMessage.unapply))
}

case class SendMailRequest(messages: Seq[SendMailMessage],
                           sandbox: Boolean = false)

object SendMailRequest {

  implicit val writes: Writes[SendMailRequest] = (
    (JsPath \ "Messages").write[Seq[SendMailMessage]] and
      (JsPath \ "SandboxMode").write[Boolean]
    ) (unlift(SendMailRequest.unapply))
}
