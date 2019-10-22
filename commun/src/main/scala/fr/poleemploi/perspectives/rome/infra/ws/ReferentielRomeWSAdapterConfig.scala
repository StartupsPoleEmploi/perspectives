package fr.poleemploi.perspectives.rome.infra.ws

import fr.poleemploi.perspectives.commun.infra.oauth.OauthConfig

case class ReferentielRomeWSAdapterConfig(urlApi: String,
                                          oauthConfig: OauthConfig) {

  val clientId: String = oauthConfig.clientId

  val clientSecret: String = oauthConfig.clientSecret

  val urlAuthentification: String = oauthConfig.urlAuthentification

  val realm: String = oauthConfig.realm
}
