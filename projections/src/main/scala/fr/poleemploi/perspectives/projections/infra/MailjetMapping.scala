package fr.poleemploi.perspectives.projections.infra

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

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

case class MailjetMessage(from: MailjetSender,
                          to: List[MailjetRecipient],
                          subject: String,
                          textPart: String,
                          htmlPart: String)

object MailjetMessage {

  implicit val mailjetMessageWrites: Writes[MailjetMessage] = (
    (JsPath \ "From").write[MailjetSender] and
      (JsPath \ "To").write[List[MailjetRecipient]] and
      (JsPath \ "Subject").write[String] and
      (JsPath \ "TextPart").write[String] and
      (JsPath \ "HTMLPart").write[String]
    ) (unlift(MailjetMessage.unapply))
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

case class MailjetEmail(messages: List[MailjetMessage])

case class MailjetTemplateEmail(messages: List[MailjetTemplateMessage])