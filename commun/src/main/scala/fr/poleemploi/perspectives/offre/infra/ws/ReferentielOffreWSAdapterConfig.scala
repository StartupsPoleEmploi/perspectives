package fr.poleemploi.perspectives.offre.infra.ws

import fr.poleemploi.perspectives.commun.infra.oauth.OauthConfig

case class ReferentielOffreWSAdapterConfig(urlApi: String,
                                           oauthConfig: OauthConfig) {

  val clientId: String = oauthConfig.clientId

  val clientSecret: String = oauthConfig.clientSecret

  val urlAuthentification: String = oauthConfig.urlAuthentification

  val realm: String = oauthConfig.realm
}
