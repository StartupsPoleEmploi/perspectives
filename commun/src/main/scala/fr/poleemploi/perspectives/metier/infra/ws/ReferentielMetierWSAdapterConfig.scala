package fr.poleemploi.perspectives.metier.infra.ws

import fr.poleemploi.perspectives.commun.infra.oauth.OauthConfig

case class ReferentielMetierWSAdapterConfig(urlApi: String,
                                            oauthConfig: OauthConfig) {

  val clientId: String = oauthConfig.clientId

  val clientSecret: String = oauthConfig.clientSecret

  val urlAuthentification: String = oauthConfig.urlAuthentification

  val realm: String = oauthConfig.realm
}
