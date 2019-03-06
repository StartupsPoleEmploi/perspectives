package fr.poleemploi.perspectives.offre.infra.ws

import fr.poleemploi.perspectives.commun.infra.ws.{WSAdapter, WebServiceException}
import fr.poleemploi.perspectives.metier.infra.ws.AccessTokenResponse
import fr.poleemploi.perspectives.offre.domain.{CriteresRechercheOffre, Offre, ReferentielOffre}
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

  /**
    * L'API est limitée en nombre d'appels par seconde, il faut donc gérer le statut 429. <br />
    * Ne gère que 3 CodeROME pour l'instant (découpage des requêtes à refaire pour en gérer plus)
    */
  def rechercherOffres(criteres: CriteresRechercheOffre): Future[List[Offre]] = {
    def callWS(accessTokenResponse: AccessTokenResponse, request: RechercheOffreRequest): Future[List[Offre]] =
      wsClient.url(s"${config.urlApi}/offresdemploi/v2/offres/search")
        .addQueryStringParameters(request.params: _ *)
        .addHttpHeaders(
          ("Authorization", s"Bearer ${accessTokenResponse.accessToken}"),
          jsonContentType,
          ("Accept", "application/json")
        )
        .get()
        .flatMap(r => filtreStatutReponse(response = r, statutNonGere = s => s != 200 && s != 206))
        .map(r =>
          (r.json \ "resultats").as[JsArray].value.map(_.as[OffreResponse])
            .flatMap(offreResponse => mapping.buildOffre(criteres, offreResponse)).toList
        )
        .recoverWith {
          case e: WebServiceException if e.statut == 429 =>
            referentielOffreWSLogger.error(e.getMessage)
            Future.successful(Nil)
        }

    for {
      accessTokenResponse <- genererAccessToken
      codeInsee <- criteres.codePostal
        .map(c => codeInsee(accessTokenResponse, c).map(Some(_)))
        .getOrElse(Future.successful(None))
      offres <- callWS(
        accessTokenResponse = accessTokenResponse,
        request = mapping.buildRechercherOffresRequest(criteres, codeInsee)
      )
    } yield {
      offres
        .distinct
        .sortWith((o1, o2) => o1.dateActualisation.isAfter(o2.dateActualisation))
    }
  }

  private def genererAccessToken: Future[AccessTokenResponse] =
    wsClient
      .url(s"${config.urlAuthentification}/connexion/oauth2/access_token?realm=%2F${config.realm}")
      .post(Map(
        "grant_type" -> "client_credentials",
        "client_id" -> config.clientId,
        "client_secret" -> config.clientSecret,
        "scope" -> s"application_${config.clientId} ${config.scopes.mkString(" ")}",
      ))
      .flatMap(filtreStatutReponse(_))
      .map(_.json.as[AccessTokenResponse])

  private def codeInsee(accessTokenResponse: AccessTokenResponse, codePostal: String): Future[String] =
    cacheApi
      .getOrElseUpdate(cacheKeyCommunes)(listerCommunes(accessTokenResponse))
      .map(_.getOrElse(codePostal match {
        case "75000" => "75001"
        case "69000" => "69001"
        case "13000" => "13001"
        case c@_ => c
      }, throw new IllegalArgumentException(s"Aucun codeINSEE associé au codePostal : $codePostal")))

  private def listerCommunes(accessTokenResponse: AccessTokenResponse): Future[Map[String, String]] =
    for {
      communes <- wsClient
        .url(s"${config.urlApi}/offresdemploi/v2/referentiel/communes")
        .addHttpHeaders(
          ("Authorization", s"Bearer ${accessTokenResponse.accessToken}"),
          jsonContentType
        )
        .get()
        .map(r => r.json.as[List[CommuneResponse]].map(c => c.codePostal -> c.code).toMap)
    } yield communes

}
