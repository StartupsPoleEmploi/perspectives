package fr.poleemploi.perspectives.emailing.infra.ws

import java.time.format.DateTimeFormatter

import fr.poleemploi.perspectives.candidat.Adresse
import fr.poleemploi.perspectives.commun.domain.{Email, Genre}
import fr.poleemploi.perspectives.emailing.domain._
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetContactId
import play.api.libs.functional.syntax._
import play.api.libs.json._

class MailjetWSMapping(testeurs: List[Email]) {

  import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetContactProperties._

  val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

  val idListeCandidatsInscrits: Int = 9908
  val idListeCandidatsProspects: Int = 10066519

  val idListeRecruteursInscrits: Int = 9909
  val idListeRecruteursProspects: Int = 10145914

  val idListeTesteurs: Int = 10145941

  def buildRequestInscriptionCandidat(candidatInscrit: CandidatInscrit): InscriptionRequest = InscriptionRequest(
    idListe = filtrerListeTesteurs(idListeCandidatsInscrits, candidatInscrit.email),
    request = buildContactRequestInscriptionCandidat(candidatInscrit)
  )

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

  def buildContactListsRequestInscriptionCandidat: ManageContactListsRequest =
    ManageContactListsRequest(List(
      ContactList(listID = s"$idListeCandidatsProspects", action = "remove")
    ))

  def buildRequestInscriptionRecruteur(recruteurInscrit: RecruteurInscrit): InscriptionRequest = InscriptionRequest(
    idListe = filtrerListeTesteurs(idListeRecruteursInscrits, recruteurInscrit.email),
    request = buildContactRequestInscriptionRecruteur(recruteurInscrit)
  )

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

  def buildContactListsRequestInscriptionRecruteur: ManageContactListsRequest =
    ManageContactListsRequest(List(
      ContactList(listID = s"$idListeRecruteursProspects", action = "remove")
    ))

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

  def buildRequestImportProspectsCandidats(prospectsCandidats: Stream[MRSValideeProspectCandidat]): ManageManyContactsRequest =
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
          mrs_date -> s"${p.dateEvaluation.atStartOfDay().format(formatter)}"
        )
      )).toList,
      contactsLists = List(ContactList(
        listID = s"$idListeCandidatsProspects",
        action = "addnoforce"
      ))
    )

  private def buildGenre(genre: Genre): String = genre match {
    case Genre.HOMME => "M."
    case Genre.FEMME => "Mme"
    case g@_ => throw new IllegalArgumentException(s"Genre inconnu : $g")
  }

  private def filtrerListeTesteurs(idListe: Int, email: Email): Int =
    if (testeurs.contains(email)) idListeTesteurs else idListe
}

case class InscriptionRequest(idListe: Int,
                              request: ManageContactRequest)

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