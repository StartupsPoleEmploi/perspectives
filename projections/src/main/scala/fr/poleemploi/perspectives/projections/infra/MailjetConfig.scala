package fr.poleemploi.perspectives.projections.infra

case class MailjetConfig(urlApi: String,
                         senderAdress: String,
                         apiKeyPublic: String,
                         apiKeyPrivate: String)
