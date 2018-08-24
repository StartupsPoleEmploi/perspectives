package fr.poleemploi.perspectives.domain.emailing.infra.mailjet

case class MailjetAdapterConfig(urlApi: String,
                                senderAdress: String,
                                apiKeyPublic: String,
                                apiKeyPrivate: String,
                                testeurs: List[String])
