package fr.poleemploi.perspectives.emailing.infra.ws

import fr.poleemploi.perspectives.commun.domain.Email

case class MailjetWSAdapterConfig(urlApi: String,
                                  senderAdress: Email,
                                  apiKeyPublic: String,
                                  apiKeyPrivate: String)
