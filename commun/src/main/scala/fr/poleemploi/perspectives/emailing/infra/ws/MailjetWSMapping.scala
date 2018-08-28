package fr.poleemploi.perspectives.emailing.infra.ws

import fr.poleemploi.perspectives.commun.domain.Genre
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetContactId
import play.api.libs.functional.syntax._
import play.api.libs.json._

object MailjetMapping {

  def serializeGenre(genre: Genre): String = genre match {
    case Genre.HOMME => "M."
    case Genre.FEMME => "Mme"
    case g@_ => throw new IllegalArgumentException(s"Genre inconnu : $g")
  }
}

case class MailjetRecipient(email: String,
                            name: String)

object MailjetRecipient {

  implicit val mailjetRecipientWrites: Writes[MailjetRecipient] = (
    (JsPath \ "Email").write[String] and
      (JsPath \ "Name").write[String]
    ) (unlift(MailjetRecipient.unapply))
}

case class MailjetSender(email: String,
                         name: String)

object MailjetSender {

  implicit val mailjetSenderWrites: Writes[MailjetSender] = (
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

  implicit val mailjetTemplateWrites: Writes[MailjetTemplateMessage] = (
    (JsPath \ "From").write[MailjetSender] and
      (JsPath \ "To").write[List[MailjetRecipient]] and
      (JsPath \ "Subject").write[String] and
      (JsPath \ "TemplateID").write[Int] and
      (JsPath \ "TemplateLanguage").write[Boolean] and
      (JsPath \ "Variables").write[Map[String, String]]
    ) (unlift(MailjetTemplateMessage.unapply))
}

case class MailjetTemplateEmail(messages: List[MailjetTemplateMessage])

case class ManageContactRequest(email: String,
                                name: Option[String] = None,
                                action: String,
                                properties: JsValue)

object ManageContactRequest {

  implicit val manageContactRequestWrites: Writes[ManageContactRequest] = (
    (JsPath \ "Email").write[String] and
      (JsPath \ "Name").write[Option[String]] and
      (JsPath \ "Action").write[String] and
      (JsPath \ "Properties").write[JsValue]
    ) (unlift(ManageContactRequest.unapply))
}

case class ManageContactResponseData(contactId: MailjetContactId,
                                     email: String)

object ManageContactResponseData {

  implicit val manageContactResponseDataReads: Reads[ManageContactResponseData] = (
    (JsPath \ "ContactID").read[Int].map(MailjetContactId) and
      (JsPath \ "Email").read[String]
    ) (ManageContactResponseData.apply _)
}

case class ManageContactResponse(count: Int,
                                 datas: List[ManageContactResponseData]) {

  def contactId: MailjetContactId = datas.head.contactId
}

object ManageContactResponse {

  implicit val manageContactReponseReads: Reads[ManageContactResponse] = (
    (JsPath \ "Count").read[Int] and
      (JsPath \ "Data").read[List[ManageContactResponseData]]
    ) (ManageContactResponse.apply _)

}
