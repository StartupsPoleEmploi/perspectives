package fr.poleemploi.perspectives.emailing.infra.ws

import java.time.format.DateTimeFormatter
import java.util.Locale

import fr.poleemploi.perspectives.commun.domain.{Departement, Email, Genre}
import fr.poleemploi.perspectives.emailing.domain._
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetContactId
import fr.poleemploi.perspectives.recruteur.alerte.domain.FrequenceAlerte
import play.api.libs.functional.syntax._
import play.api.libs.json._

class MailjetWSMapping {

  val dateTimeFormatterAlerteMailRecruteur: DateTimeFormatter = DateTimeFormatter.ofPattern("eeee d MMMM yyyy", Locale.FRANCE)

  def buildRequestCandidatInscrit(candidatInscrit: CandidatInscrit): ManageContactRequest =
    ManageContactRequest(
      email = candidatInscrit.email.value,
      name = Some(s"${candidatInscrit.nom.capitalize} ${candidatInscrit.prenom.capitalize}"),
      action = "addnoforce",
      properties = Json.obj(
        "nom" -> candidatInscrit.nom.capitalize,
        "prénom" -> candidatInscrit.prenom.capitalize, // doit comporter l'accent
        "genre" -> serializeGenre(candidatInscrit.genre),
        "cv" -> false
      )
    )

  def buildRequestRecruteurInscrit(recruteurInscrit: RecruteurInscrit): ManageContactRequest =
    ManageContactRequest(
      email = recruteurInscrit.email.value,
      name = Some(s"${recruteurInscrit.nom.capitalize} ${recruteurInscrit.prenom.capitalize}"),
      action = "addnoforce",
      properties = Json.obj(
        "nom" -> recruteurInscrit.nom.capitalize,
        "prénom" -> recruteurInscrit.prenom.capitalize, // doit comporter l'accent
        "genre" -> serializeGenre(recruteurInscrit.genre)
      )
    )

  def buildRequestMiseAJourCV(email: Email, possedeCV: Boolean): ManageContactRequest =
    ManageContactRequest(
      email = email.value,
      action = "addnoforce",
      properties = Json.obj(
        "cv" -> possedeCV
      )
    )

  def buildAlerteMailTemplateRecruteur(alerteMailRecruteur: AlerteMailRecruteur,
                                       templateId: Int,
                                       sender: String): MailjetTemplateEmail =
    MailjetTemplateEmail(
      messages = List(MailjetTemplateMessage(
        from = MailjetSender(email = sender, name = ""),
        to = List(MailjetRecipient(email = alerteMailRecruteur.email.value, name = "")),
        subject = subjectAlerteMailRecruteur(alerteMailRecruteur),
        templateID = templateId,
        templateLanguage = true,
        variables = variablesAlerteMailRecruteur(alerteMailRecruteur)
      ))
    )

  private def subjectAlerteMailRecruteur(alerteMailRecruteur: AlerteMailRecruteur): String = {
    def nbCandidats(nbCandidats: Int): String = nbCandidats match {
      case x if x <= 0 => ""
      case x if x == 1 => s"1 nouveau candidat"
      case x if x > 1 => s"$x nouveaux candidats"
    }

    alerteMailRecruteur match {
      case a: AlerteMailRecruteurDepartement =>
        s"${nbCandidats(a.nbCandidats)} en ${a.departement.label}"
      case a: AlerteMailRecruteurSecteur =>
        s"${nbCandidats(a.nbCandidats)} dans le secteur ${a.secteurActivite.label}${departement(a.departement)}"
      case a: AlerteMailRecruteurMetier =>
        s"${nbCandidats(a.nbCandidats)} sur le métier ${a.metier.label}${departement(a.departement)}"
    }
  }

  private def variablesAlerteMailRecruteur(alerteMailRecruteur: AlerteMailRecruteur): Map[String, String] = {
    def nbCandidats(nbCandidats: Int): String = nbCandidats match {
      case x if x <= 0 => ""
      case x if x == 1 => s"1 nouveau candidat s'est inscrit"
      case x if x > 1 => s"$x nouveaux candidats se sont inscrits"
    }

    def dateRechercheCandidat(alerteMailRecruteur: AlerteMailRecruteur): String = alerteMailRecruteur.frequence match {
      case FrequenceAlerte.HEBDOMADAIRE => s"depuis le ${dateTimeFormatterAlerteMailRecruteur.format(alerteMailRecruteur.apresDateInscription)}"
      case _ => ""
    }

    alerteMailRecruteur match {
      case a: AlerteMailRecruteurDepartement =>
        Map(
          "texteInscription" -> s"${nbCandidats(a.nbCandidats)} en ${a.departement.label} ${dateRechercheCandidat(a)}",
          "lienConnexion" -> a.lienConnexion
        )
      case a: AlerteMailRecruteurSecteur =>
        Map(
          "texteInscription" -> s"${nbCandidats(a.nbCandidats)} dans le secteur ${a.secteurActivite.label}${departement(a.departement)} ${dateRechercheCandidat(a)}",
          "lienConnexion" -> a.lienConnexion
        )
      case a: AlerteMailRecruteurMetier =>
        Map(
          "texteInscription" -> s"${nbCandidats(a.nbCandidats)} sur le métier ${a.metier.label}${departement(a.departement)} ${dateRechercheCandidat(a)}",
          "lienConnexion" -> a.lienConnexion
        )
    }
  }

  private def departement(departement: Option[Departement]): String =
    departement.map(d => s" en ${d.label}").getOrElse("")

  private def serializeGenre(genre: Genre): String = genre match {
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
