package fr.poleemploi.perspectives.offre.infra.ws

import fr.poleemploi.perspectives.commun.infra.oauth.OauthConfig
import fr.poleemploi.perspectives.commun.infra.ws.{AccessToken, WSAdapter, WebServiceException}
import fr.poleemploi.perspectives.metier.infra.ws.AccessTokenResponse
import fr.poleemploi.perspectives.offre.domain._
import play.api.cache.AsyncCacheApi
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferentielOffreWSAdapter(config: ReferentielOffreWSAdapterConfig,
                                wsClient: WSClient,
                                cacheApi: AsyncCacheApi,
                                mapping: ReferentielOffreWSMapping) extends ReferentielOffre with WSAdapter {

  private val cacheKeyCommunes = "referentielOffre.communes"
  private val cacheKeyAccessToken = "referentielOffre.accessToken"

  /**
    * L'API est limitée en nombre d'appels par seconde, il faut donc gérer le statut 429. <br />
    * Il faut aussi gérer le statut 206 (Partial Content) pour prendre en compte le fait qu'on ne récupère pas tous les résultats en une fois.
    */
  def rechercherOffres(criteres: CriteresRechercheOffre): Future[RechercheOffreResult] = {
    def callWS(accessToken: AccessToken, params: List[(String, String)]): Future[RechercheOffreResult] =
      wsClient.url(s"${config.urlApi}/offresdemploi/v2/offres/search")
        .addQueryStringParameters(params: _ *)
        .addHttpHeaders(authorizationBearer(accessToken), jsonContentType)
        .get()
        .flatMap(r => filtreStatutReponse(response = r, statutNonGere = s => s != 200 && s != 206))
        .map(r =>
          // FIXME : Si 206 mais qu'on a pas assez d'offres après avoir filtrer pour remplir au moins une page, alors on rappelle l'API et on recalcule
          RechercheOffreResult(
            offres = mapping.filterOffresResponses(
              criteresRechercheOffre = criteres,
              offres = (r.json \ "resultats").as[List[OffreResponse]]).map(mapping.buildOffre),
            pageSuivante =
              if (r.status == 206)
                mapping.buildPageOffres(r.header("Content-Range"), r.header("Accept-Range"))
              else
                None
          )
        ).recoverWith {
        case e: WebServiceException if e.statut == 429 =>
          referentielOffreWSLogger.error(e.getMessage)
          Future.successful(RechercheOffreResult(
            offres = Nil,
            pageSuivante = None
          ))
      }

    for {
      accessToken <- getAccessToken
      codeInsee <- criteres.codePostal
        .map(c => codeInsee(accessToken, c).map(Some(_)))
        .getOrElse(Future.successful(None))
      rechercheOffresResult <- callWS(
        accessToken = accessToken,
        params = mapping.buildRechercherOffresRequest(criteres, codeInsee)
      )
    } yield rechercheOffresResult
  }

  private def getAccessToken: Future[AccessToken] =
    for {
      cachedAccessToken <- cacheApi.get[AccessToken](cacheKeyAccessToken)
      accessToken <- cachedAccessToken.map(Future.successful).getOrElse {
        genererAccessToken.map(accessTokenResponse => {
          /*cacheApi.set(
            key = cacheKeyAccessToken,
            value = accessTokenResponse.accessToken,
            expiration = accessTokenResponse.expiresIn - 10.seconds
          )*/
          accessTokenResponse.accessToken
        })
      }
    } yield accessToken

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

  private def codeInsee(accessToken: AccessToken, codePostal: String): Future[String] =
    cacheApi
      .getOrElseUpdate(cacheKeyCommunes)(listerCommunes(accessToken))
      .map(_.getOrElse(codePostal match {
        case "75000" => "75001"
        case "69000" => "69001"
        case "13000" => "13001"
        case c@_ => c
      }, throw new IllegalArgumentException(s"Aucun codeINSEE associé au codePostal : $codePostal")))

  private def listerCommunes(accessToken: AccessToken): Future[Map[String, String]] =
    for {
      communes <- wsClient
        .url(s"${config.urlApi}/offresdemploi/v2/referentiel/communes")
        .addHttpHeaders(authorizationBearer(accessToken), jsonContentType)
        .get()
        .flatMap(filtreStatutReponse(_))
        .map(r => r.json.as[List[CommuneResponse]].map(c => c.codePostal -> c.code).toMap)
    } yield communes

}
