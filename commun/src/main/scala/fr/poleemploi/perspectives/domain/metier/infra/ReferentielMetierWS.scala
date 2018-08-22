package fr.poleemploi.perspectives.domain.metier.infra

import fr.poleemploi.perspectives.domain.metier.ReferentielMetier
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class ReferentielMetierWSException(message: String) extends Exception(message)

class ReferentielMetierWS(referentielMetierWSConfig: ReferentielMetierWSConfig,
                          wsClient: WSClient) extends ReferentielMetier {

  def getMetiers: Future[Unit] = {
    genererAccessToken.flatMap(accessTokenResponse => {
      load(accessTokenResponse)
    })
  }

  def genererAccessToken: Future[AccessTokenResponse] =
    wsClient
      .url(s"${referentielMetierWSConfig.urlAuthentification}/connexion/oauth2/access_token?realm=%2Fpartenaire")
      .post(Map(
        "grant_type" -> "client_credentials",
        "client_id" -> referentielMetierWSConfig.clientId,
        "client_secret" -> referentielMetierWSConfig.clientSecret,
        "scope" -> s"application_${referentielMetierWSConfig.clientId} api_infotravailv1",
      ))
      .map(filtreStatutReponse(_))
      .map(_.json.as[AccessTokenResponse])

  def load(accessTokenResponse: AccessTokenResponse): Future[Unit] =
    wsClient
      .url(s"${referentielMetierWSConfig.urlApi}/infotravail/v1/datastore_search?resource_id=2cbf766c-f9a1-49f4-a17f-aeeb06ceb834")
      .addHttpHeaders(("Authorization", s"Bearer ${accessTokenResponse.accessToken}"))
      .get()
      //.map(filtreStatutReponse(_))
      .map { response =>
        println(response.body)
      }

  private def filtreStatutReponse(response: WSResponse,
                                  statutErreur: Int => Boolean = s => s >= 400,
                                  statutNonGere: Int => Boolean = s => s != 200): WSResponse = response.status match {
    case s if statutErreur(s) => throw ReferentielMetierWSException(s"Erreur lors de l'appel au référentiel métier. Code: ${response.status}. Reponse : ${response.body}")
    case s if statutNonGere(s) => throw ReferentielMetierWSException(s"Statut non géré lors de l'appel au référentiel métier. Code: ${response.status}. Reponse : ${response.body}")
    case _ => response
  }
}
