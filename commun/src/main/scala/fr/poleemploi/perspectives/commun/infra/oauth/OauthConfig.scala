package fr.poleemploi.perspectives.commun.infra.oauth

case class OauthConfig(clientId: String,
                       clientSecret: String,
                       urlAuthentification: String,
                       realm: String)
