package authentification.infra.peconnect

import fr.poleemploi.perspectives.infra.oauth.OAuthConfig

case class PEConnectRecruteurConfig(urlAuthentification: String,
                                    urlApi: String,
                                    oauthConfig: OAuthConfig) {

  val clientId: String = oauthConfig.clientId

  val clientSecret: String = oauthConfig.clientSecret
}

case class PEConnectCandidatConfig(urlAuthentification: String,
                                   urlApi: String,
                                   oauthConfig: OAuthConfig) {

  val clientId: String = oauthConfig.clientId

  val clientSecret: String = oauthConfig.clientSecret
}