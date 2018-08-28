package fr.poleemploi.perspectives.emailing.infra.ws

case class MailjetWSAdapterConfig(urlApi: String,
                                  senderAdress: String,
                                  apiKeyPublic: String,
                                  apiKeyPrivate: String,
                                  testeurs: List[String])
