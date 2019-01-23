package fr.poleemploi.perspectives.offre.infra.ws

import fr.poleemploi.perspectives.commun.domain.CodeROME
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
    * L'API ne gère que 3 codesROME par appel, on découpe donc en plusieurs appels. <br />
    * Elle est également limitée en nombre d'appels par seconde, il faut donc gérer le statut 429
    */
  def rechercherOffres(criteres: CriteresRechercheOffre): Future[List[Offre]] = {
    def callWS(accessTokenResponse: AccessTokenResponse, codesROME: List[CodeROME] = Nil, codeInsee: String): Future[List[Offre]] =
      wsClient.url(s"${config.urlApi}/offresdemploi/v2/offres/search")
        .addQueryStringParameters(
          "codeROME" -> codesROME.map(_.value).mkString(","),
          "experience" -> mapping.buildExperience(criteres.experience),
          "commune" -> codeInsee,
          "distance" -> s"${criteres.rayonRecherche.value}",
        )
        .addHttpHeaders(
          ("Authorization", s"Bearer ${accessTokenResponse.accessToken}"),
          jsonContentType,
          ("Accept", "application/json")
        )
        .get()
        .flatMap(r => filtreStatutReponse(response = r, statutNonGere = s => s != 200 && s != 206))
        .map(r =>
          (r.json \ "resultats").as[JsArray].value.map(_.as[OffreResponse])
            .map(mapping.buildOffre).toList
        )
        .recoverWith {
          case e: WebServiceException if e.statut == 429 =>
            referentielOffreWSLogger.error(e.getMessage)
            Future.successful(Nil)
        }

    for {
      accessTokenResponse <- genererAccessToken
      codeInsee <- codeInsee(criteres.codePostal, accessTokenResponse)
      offres <- Future.sequence(criteres.codesROME.sliding(3, 3).toList.map(codesROME =>
        callWS(accessTokenResponse, codesROME, codeInsee)
      ))
    } yield {
      offres
        .flatten
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
        "scope" -> s"application_${config.clientId} api_offresdemploiv2 qos_silver_offresdemploiv2 o2dsoffre",
      ))
      .flatMap(filtreStatutReponse(_))
      .map(_.json.as[AccessTokenResponse])

  private def codeInsee(codePostal: String, accessTokenResponse: AccessTokenResponse): Future[String] =
    cacheApi
      .getOrElseUpdate(cacheKeyCommunes)(listerCommunes(accessTokenResponse))
      .map(_.getOrElse(codePostal, throw new IllegalArgumentException(s"Aucun codeInsee associé au codePostal : $codePostal")))

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
