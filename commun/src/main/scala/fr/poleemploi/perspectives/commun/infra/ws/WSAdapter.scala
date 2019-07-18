package fr.poleemploi.perspectives.commun.infra.ws

import play.api.libs.ws.WSResponse

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
}
