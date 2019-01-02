package fr.poleemploi.perspectives.metier.infra.ws

import fr.poleemploi.perspectives.commun.domain.{CodeROME, Metier}
import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

case class ReferentielMetierWSException(message: String) extends Exception(message)

/**
  * Se base sur l'API infotravail de l'emploi store
  */
class ReferentielMetierWSAdapter(config: ReferentielMetierWSAdapterConfig,
                                 wsClient: WSClient) extends ReferentielMetier with WSAdapter {

  val metiersResourceId = "767d0c4a-277b-493c-84b7-00143933efce"

  // FIXME : charger en non bloquant
  val metiers: Map[CodeROME, Metier] = Await.result(genererAccessToken.flatMap(listerMetiers), 10.seconds)

  /**
    * Renvoie une exception si le Métier n'est associé à aucun code.
    */
  override def metierParCode(code: CodeROME): Metier =
    metiers.getOrElse(code, throw new IllegalArgumentException(s"Aucun label associé au code : $code"))

  private def genererAccessToken: Future[AccessTokenResponse] =
    wsClient
      .url(s"${config.urlAuthentification}/connexion/oauth2/access_token?realm=%2F${config.realm}")
      .post(Map(
        "grant_type" -> "client_credentials",
        "client_id" -> config.clientId,
        "client_secret" -> config.clientSecret,
        "scope" -> s"application_${config.clientId} api_infotravailv1",
      ))
      .flatMap(filtreStatutReponse(_))
      .map(_.json.as[AccessTokenResponse])

  private def listerMetiers(accessTokenResponse: AccessTokenResponse): Future[Map[CodeROME, Metier]] = {
    def callWS(offset: Option[Int] = None): Future[ListeMetiersResponse] =
      wsClient
        .url(s"${config.urlApi}/infotravail/v1/datastore_search" +
          offset.map(o => s"?offset=$o&resource_id=$metiersResourceId")
            .getOrElse(s"?resource_id=$metiersResourceId"))
        .addHttpHeaders(("Authorization", s"Bearer ${accessTokenResponse.accessToken}"))
        .get()
        .flatMap(filtreStatutReponse(_))
        .map(_.json.as[ListeMetiersResponse])

    val futures = callWS().flatMap { listeMetiers =>
      val nbRecordsParPage = listeMetiers.records.size
      val nbPages = Math.floor(listeMetiers.total / nbRecordsParPage)

      Future.sequence(Future.successful(listeMetiers) :: List.tabulate(nbPages.toInt)(n =>
        callWS(offset = Some((n + 1) * nbRecordsParPage))
      ))
    }

    futures.map { l =>
      l.flatMap(_.records)
        .foldLeft(Map[CodeROME, Metier]())(
          (map, romeCardResponse) => map + (CodeROME(romeCardResponse.romeProfessionCardCode) -> romeCardResponse.toMetier)
        )
    }
  }
}
