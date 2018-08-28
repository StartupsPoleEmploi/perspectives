package fr.poleemploi.perspectives.authentification.infra.ws

import fr.poleemploi.perspectives.commun.infra.oauth.OauthConfig

case class PEConnectWSAdapterConfig(urlAuthentification: String,
                                    urlApi: String,
                                    oauthConfig: OauthConfig) {

  val clientId: String = oauthConfig.clientId

  val clientSecret: String = oauthConfig.clientSecret
}