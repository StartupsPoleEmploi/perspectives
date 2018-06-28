package authentification.infra.peconnect

case class OAuthConfig(clientId: String,
                       clientSecret: String)

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