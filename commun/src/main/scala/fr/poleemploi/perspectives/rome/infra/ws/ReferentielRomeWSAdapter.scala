package fr.poleemploi.perspectives.rome.infra.ws

import fr.poleemploi.perspectives.commun.domain.CodeROME
import fr.poleemploi.perspectives.commun.infra.oauth.OauthConfig
import fr.poleemploi.perspectives.commun.infra.ws.{AccessToken, AccessTokenResponse, WSAdapter}
import fr.poleemploi.perspectives.rome.domain.{Appellation, ReferentielRome}
import fr.poleemploi.perspectives.rome.infra.elasticsearch.ReferentielRomeElasticsearchAdapter
import play.api.cache.AsyncCacheApi
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Se base sur l'API ROME de l'emploi store. <br />
  * Charge le contenu des appellations dans un index ES pour permettre de rechercher les metiers par appellation
  */
class ReferentielRomeWSAdapter(config: ReferentielRomeWSAdapterConfig,
                               mapping: ReferentielRomeWSMapping,
                               cacheApi: AsyncCacheApi,
                               wsClient: WSClient,
                               elasticsearchAdapter: ReferentielRomeElasticsearchAdapter) extends ReferentielRome with WSAdapter {

  override def rechargerAppellations: Future[Unit] =
    for {
      accessTokenResponse <- genererAccessToken
      res <- listerAppellationsEtChargerIndex(accessTokenResponse.accessToken)
    } yield res.map(_ => ())

  override def appellationsRecherche(query: String): Future[Seq[Appellation]] =
    elasticsearchAdapter.appellationsRecherche(query)

  private def genererAccessToken: Future[AccessTokenResponse] =
    wsClient
      .url(s"${config.urlAuthentification}/connexion/oauth2/access_token?realm=%2F${config.realm}")
      .post(Map(
        "grant_type" -> "client_credentials",
        "client_id" -> config.clientId,
        "client_secret" -> config.clientSecret,
        "scope" -> OauthConfig.scopes(config.oauthConfig),
      ))
      .flatMap(filtreStatutReponse(_))
      .map(_.json.as[AccessTokenResponse])

  private def listerAppellationsEtChargerIndex(accessToken: AccessToken): Future[Seq[Unit]] = {
    def callWS(codeROME: CodeROME): Future[Seq[AppellationResponse]] =
      wsClient
        .url(s"${config.urlApi}/rome/v1/metier/${codeROME.value}/appellation")
        .addHttpHeaders(authorizationBearer(accessToken))
        .get()
        .flatMap(filtreStatutReponse(_))
        .map(_.json.as[Seq[AppellationResponse]])

    Future.sequence(ReferentielRome.codesRomes.map { codeROME =>
      Thread.sleep(1500) // On est limites a 1 req / seconde...
      for {
        appellationsResponse <- callWS(codeROME)
        appellations = appellationsResponse.map(response => mapping.buildAppellation(codeROME, response))
        res <- elasticsearchAdapter.indexAppellations(appellations)
      } yield res
    })
  }

}


