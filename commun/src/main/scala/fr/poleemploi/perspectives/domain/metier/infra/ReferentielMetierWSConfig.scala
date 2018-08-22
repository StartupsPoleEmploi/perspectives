package fr.poleemploi.perspectives.domain.metier.infra

import fr.poleemploi.perspectives.infra.oauth.OAuthConfig

case class ReferentielMetierWSConfig(urlAuthentification: String,
                                     urlApi: String,
                                     oauthConfig: OAuthConfig) {

  val clientId: String = oauthConfig.clientId

  val clientSecret: String = oauthConfig.clientSecret
}
