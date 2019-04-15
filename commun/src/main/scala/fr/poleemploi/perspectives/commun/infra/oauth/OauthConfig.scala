package fr.poleemploi.perspectives.commun.infra.oauth

case class OauthConfig(clientId: String,
                       clientSecret: String,
                       urlAuthentification: String,
                       realm: String,
                       scopes: List[OauthScope])

object OauthConfig {

  def scopes(oauthConfig: OauthConfig): String = s"application_${oauthConfig.clientId} ${oauthConfig.scopes.map(_.value).mkString(" ")}"
}
