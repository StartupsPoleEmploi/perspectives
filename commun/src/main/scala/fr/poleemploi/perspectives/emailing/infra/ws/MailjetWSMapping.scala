package fr.poleemploi.perspectives.emailing.infra.ws

import fr.poleemploi.perspectives.candidat.Adresse
import fr.poleemploi.perspectives.commun.domain.{Email, Genre}
import fr.poleemploi.perspectives.emailing.domain._
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetContactId
import play.api.libs.functional.syntax._
import play.api.libs.json._

class MailjetWSMapping(testeurs: List[Email]) {

  val idListeCandidatsInscrits: Int = 9908
  val idListeCandidatsProspects: Int = 10066519

  val idListeRecruteursInscrits: Int = 9909
  val idListeRecruteursProspects: Int = 10145914

  val idListeTesteurs: Int = 20603

  def buildContactRequestInscriptionCandidat(candidatInscrit: CandidatInscrit): UpdateContactDataRequest =
    UpdateContactDataRequest(
      properties = List(
        Json.obj("Name" -> "nom", "Value" -> candidatInscrit.nom.value),
        Json.obj("Name" -> "prénom", "Value" -> candidatInscrit.prenom.value), // le nom de l'attribut doit comporter l'accent
        Json.obj("Name" -> "genre", "Value" -> buildGenre(candidatInscrit.genre)),
        Json.obj("Name" -> "cv", "Value" -> false)
      )
    )

  def buildContactListsRequestInscriptionCandidat(candidatInscrit: CandidatInscrit): ManageContactListsRequest =
    ManageContactListsRequest(
      contactsList = List(
        ContactList(listID = s"$idListeCandidatsProspects", action = "remove"),
        ContactList(listID = s"${filtrerListeTesteurs(idListeCandidatsInscrits, candidatInscrit.email)}", action = "addnoforce")
      )
    )

  def buildContactRequestInscriptionRecruteur(recruteurInscrit: RecruteurInscrit): UpdateContactDataRequest =
    UpdateContactDataRequest(
      properties = List(
        Json.obj("Name" -> "nom", "Value" -> recruteurInscrit.nom.value),
        Json.obj("Name" -> "prénom", "Value" -> recruteurInscrit.prenom.value), // le nom de l'attribut doit comporter l'accent
        Json.obj("Name" -> "genre", "Value" -> buildGenre(recruteurInscrit.genre))
      )
    )

  def buildContactListsRequestInscriptionRecruteur(recruteurInscrit: RecruteurInscrit): ManageContactListsRequest =
    ManageContactListsRequest(
      contactsList = List(
        ContactList(listID = s"$idListeRecruteursProspects", action = "remove"),
        ContactList(listID = s"${filtrerListeTesteurs(idListeRecruteursInscrits, recruteurInscrit.email)}", action = "addnoforce")
      )
    )

  def buildRequestMiseAJourCVCandidat(possedeCV: Boolean): UpdateContactDataRequest =
    UpdateContactDataRequest(
      properties = List(Json.obj("Name" -> "cv", "Value" -> possedeCV))
    )

  def buildRequestMiseAJourAdresseCandidat(adresse: Adresse): UpdateContactDataRequest =
    UpdateContactDataRequest(
      properties = List(Json.obj("Name" -> "departement", "Value" -> adresse.codePostal.take(2).toInt))
    )

  private def buildGenre(genre: Genre): String = genre match {
    case Genre.HOMME => "M."
    case Genre.FEMME => "Mme"
    case g@_ => throw new IllegalArgumentException(s"Genre inconnu : $g")
  }

  private def filtrerListeTesteurs(idListe: Int, email: Email): Int =
    if (testeurs.contains(email)) idListeTesteurs else idListe
}

case class MailjetRecipient(email: String,
                            name: String)

object MailjetRecipient {

  implicit val writes: Writes[MailjetRecipient] = (
    (JsPath \ "Email").write[String] and
      (JsPath \ "Name").write[String]
    ) (unlift(MailjetRecipient.unapply))
}

case class MailjetSender(email: String,
                         name: String)

object MailjetSender {

  implicit val writes: Writes[MailjetSender] = (
    (JsPath \ "Email").write[String] and
      (JsPath \ "Name").write[String]
    ) (unlift(MailjetSender.unapply))
}

case class MailjetTemplateMessage(from: MailjetSender,
                                  to: List[MailjetRecipient],
                                  subject: String,
                                  templateID: Int,
                                  templateLanguage: Boolean,
                                  variables: Map[String, String])

object MailjetTemplateMessage {

  implicit val writes: Writes[MailjetTemplateMessage] = (
    (JsPath \ "From").write[MailjetSender] and
      (JsPath \ "To").write[List[MailjetRecipient]] and
      (JsPath \ "Subject").write[String] and
      (JsPath \ "TemplateID").write[Int] and
      (JsPath \ "TemplateLanguage").write[Boolean] and
      (JsPath \ "Variables").write[Map[String, String]]
    ) (unlift(MailjetTemplateMessage.unapply))
}

case class MailjetTemplateEmail(messages: List[MailjetTemplateMessage])

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

case class UpdateContactDataRequest(properties: List[JsValue])

object UpdateContactDataRequest {

  implicit val writes: Writes[UpdateContactDataRequest] =
    (__ \ "Data").write[List[JsValue]].contramap(_.properties)
}

case class UpdateContactDataResponse(count: Int,
                                     contactId: MailjetContactId)

object UpdateContactDataResponse {

  implicit val reads: Reads[UpdateContactDataResponse] = (
    (JsPath \ "Count").read[Int] and
      (JsPath \ "Data" \\ "ContactID").read[Long].map(MailjetContactId)
    ) (UpdateContactDataResponse.apply _)
}
