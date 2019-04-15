package fr.poleemploi.perspectives.offre.infra.ws

import fr.poleemploi.perspectives.commun.infra.oauth.OauthConfig

case class ReferentielOffreWSAdapterConfig(urlApi: String,
                                           oauthConfig: OauthConfig) {

  def clientId: String = oauthConfig.clientId

  def clientSecret: String = oauthConfig.clientSecret

  def urlAuthentification: String = oauthConfig.urlAuthentification

  def realm: String = oauthConfig.realm
}
