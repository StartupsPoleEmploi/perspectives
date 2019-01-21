package fr.poleemploi.perspectives.offre.infra.ws

import fr.poleemploi.perspectives.commun.domain.CodeROME
import fr.poleemploi.perspectives.commun.infra.ws.{WSAdapter, WebServiceException}
import fr.poleemploi.perspectives.metier.infra.ws.AccessTokenResponse
import fr.poleemploi.perspectives.offre.domain.{CriteresRechercheOffre, Offre, ReferentielOffre}
import play.api.libs.json.{JsArray, Json, Reads}
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

case class Commune(code: String,
                   codePostal: String)

object Commune {
  implicit val reads: Reads[Commune] = Json.reads[Commune]
}

class ReferentielOffreWSAdapter(config: ReferentielOffreWSAdapterConfig,
                                wsClient: WSClient,
                                mapping: ReferentielOffreWSMapping) extends ReferentielOffre with WSAdapter {

  val communes: Map[String, String] = Await.result(for {
    accessTokenResponse <- genererAccessToken
    communes <- wsClient
      .url(s"${config.urlApi}/offresdemploi/v2/referentiel/communes")
      .addHttpHeaders(
        ("Authorization", s"Bearer ${accessTokenResponse.accessToken}"),
        ("Content-Type", "application/json")
      )
      .get()
      .map(r => {
        r.json.as[List[Commune]].map(c => c.codePostal -> c.code).toMap
      })
  } yield communes, Duration.Inf)

  /**
    * L'API ne gère que 3 codesROME par appel, on découpe donc en plusieurs appels. <br />
    * Elle est également limitée en nombre d'appels par seconde, il faut donc gérer le statut 429
    */
  def rechercherOffres(criteres: CriteresRechercheOffre): Future[List[Offre]] = {
    def callWS(accessTokenResponse: AccessTokenResponse, codesROME: List[CodeROME] = Nil): Future[List[Offre]] =
      wsClient.url(s"${config.urlApi}/offresdemploi/v2/offres/search")
        .addQueryStringParameters(
          "codeROME" -> criteres.codesROME.take(3).map(_.value).mkString(","),
          "experience" -> mapping.buildExperience(criteres.experience),
          "commune" -> communes(criteres.codePostal),
          "distance" -> s"${criteres.rayonRecherche.value}",
        )
        .addHttpHeaders(
          ("Authorization", s"Bearer ${accessTokenResponse.accessToken}"),
          ("Content-Type", "application/json"),
          ("Accept", "application/json")
        )
        .get()
        .flatMap(r => filtreStatutReponse(response = r, statutNonGere = s => s != 200 && s != 206))
        .map(r => {
          (r.json \ "resultats").as[JsArray].value.map(_.as[OffreResponse])
            .map(mapping.buildOffre).toList
        })
      .recoverWith {
        case e: WebServiceException if e.statut == 429 =>
          referentielOffreWSLogger.error(e.getMessage)
          Future.successful(Nil)
      }

    for {
      accessTokenResponse <- genererAccessToken
      offres <- Future.sequence(criteres.codesROME.sliding(3, 3).toList.map(codesROME =>
        callWS(accessTokenResponse, codesROME)
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

}
