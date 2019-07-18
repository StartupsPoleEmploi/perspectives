package fr.poleemploi.perspectives.offre.infra.ws

import fr.poleemploi.perspectives.commun.infra.oauth.OauthConfig
import fr.poleemploi.perspectives.commun.infra.ws.{AccessToken, WSAdapter, WebServiceException}
import fr.poleemploi.perspectives.metier.infra.ws.AccessTokenResponse
import fr.poleemploi.perspectives.offre.domain.{CriteresRechercheOffre, RechercheOffreResult, ReferentielOffre}
import play.api.cache.AsyncCacheApi
import play.api.libs.json.JsArray
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferentielOffreWSAdapter(config: ReferentielOffreWSAdapterConfig,
                                wsClient: WSClient,
                                cacheApi: AsyncCacheApi,
                                mapping: ReferentielOffreWSMapping) extends ReferentielOffre with WSAdapter {

  private val cacheKeyCommunes = "referentielOffre.communes"

  // Nombre d'offres maximum renvoyé pour un appel à l'API
  private val offset = 150

  /**
    * L'API est limitée en nombre d'appels par seconde, il faut donc gérer le statut 429. <br />
    * Ne gère que 3 CodeROME pour l'instant (découpage des requêtes à refaire pour en gérer plus)
    */
  def rechercherOffres(criteres: CriteresRechercheOffre): Future[RechercheOffreResult] = {
    def callWS(accessToken: AccessToken, params: List[(String, String)]): Future[RechercheOffreResult] =
      wsClient.url(s"${config.urlApi}/offresdemploi/v2/offres/search")
        .addQueryStringParameters(params: _ *)
        .addHttpHeaders(authorizationBearer(accessToken), jsonContentType)
        .get()
        .flatMap(r => filtreStatutReponse(response = r, statutNonGere = s => s != 200 && s != 206))
        .map(r =>
          // On a pas le nombre de résultat total dans un seul champ : il correspond à la totalité des nbResultats contenus dans un filtre : on prend le premier qu'on trouve
          RechercheOffreResult(
            nbOffresTotal = ((r.json \ "filtresPossibles").as[JsArray].head \\ "nbResultats").map(_.as[Int]).sum,
            offres = (r.json \ "resultats").as[JsArray].value
              .map(_.as[OffreResponse])
              .flatMap(offreResponse => mapping.buildOffre(criteres, offreResponse)).toList
          )
        )
        .recoverWith {
          case e: WebServiceException if e.statut == 429 =>
            referentielOffreWSLogger.error(e.getMessage)
            Future.successful(RechercheOffreResult(
              offres = Nil,
              nbOffresTotal = 0
            ))
        }

    for {
      accessTokenResponse <- genererAccessToken
      codeInsee <- criteres.codePostal
        .map(c => codeInsee(accessTokenResponse.accessToken, c).map(Some(_)))
        .getOrElse(Future.successful(None))
      rechercheOffreResult <- callWS(
        accessToken = accessTokenResponse.accessToken,
        params = mapping.buildRechercherOffresRequest(criteres, codeInsee)
      )
    } yield {
      RechercheOffreResult(
        offres = rechercheOffreResult.offres,
        nbOffresTotal =
          if (rechercheOffreResult.nbOffresTotal <= offset)
          rechercheOffreResult.offres.size
        else
            rechercheOffreResult.nbOffresTotal
      )
    }
  }

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
