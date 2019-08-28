package fr.poleemploi.perspectives.commun.infra.ws

import play.api.libs.ws.WSResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class WebServiceException(statut: Int, message: String) extends Exception(message)

trait WSAdapter {

  def jsonContentType: (String, String) = ("Content-Type", "application/json")

  def authorizationBearer(accessToken: AccessToken): (String, String) = ("Authorization", s"Bearer ${accessToken.value}")

  def filtreStatutReponse(response: WSResponse,
                          statutErreur: Int => Boolean = s => s >= 400,
                          statutNonGere: Int => Boolean = s => s != 200 && s != 201): Future[WSResponse] =
    response.status match {
      case s if statutErreur(s) => Future.failed(WebServiceException(
        statut = response.status,
        message = s"Erreur d'appel de web service. Statut: ${response.status} (${response.statusText}). Body : ${response.body}"
      ))
      case s if statutNonGere(s) => Future.failed(WebServiceException(
        statut = response.status,
        message = s"Statut HTTP non géré. Statut: ${response.status} (${response.statusText}). Body : ${response.body}"
      ))
      case _ => Future.successful(response)
    }

  /**
    * Effectue un nombre de réessai si un web service échoue en raison d'un statut 429 (Too many requests). <br />
    * Par exemple, les services de l'emploi store sont limités en nombre de requêtes/seconde,
    * et il se peut qu'un autre client ou nous-mêmes fassions trop de requêtes et bloquions une API pour tous les autres.
    */
  def handleRateLimit(nbRetriesMax: Int = 5, sleepingTime: Long = 1000)(block: => Future[WSResponse]): Future[WSResponse] =
    if (nbRetriesMax == 0)
      Future.failed(new RuntimeException("Nombre de réessai maximum atteint"))
    else
      block recoverWith {
        case t: WebServiceException if t.statut == 429 =>
          wsLogger.warn(s"Réessai d'appel suite à un statut ${t.statut}", t)
          handleRateLimit(nbRetriesMax - 1, sleepingTime) {
            Thread.sleep(sleepingTime)
            block
          }
      }
}
