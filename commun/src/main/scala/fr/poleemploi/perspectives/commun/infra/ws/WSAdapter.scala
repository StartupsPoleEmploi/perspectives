package fr.poleemploi.perspectives.commun.infra.ws

import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class WebServiceException(message: String) extends Exception(message)

trait WSAdapter {

  def filtreStatutReponse(response: WSResponse,
                       statutErreur: Int => Boolean = s => s >= 400,
                       statutNonGere: Int => Boolean = s => s != 200 && s != 201): Future[WSResponse] =
    response.status match {
      case s if statutErreur(s) => Future.failed(new WebServiceException(s"Erreur d'appel de web service. Statut: ${response.status}. Body : ${response.body}"))
      case s if statutNonGere(s) => Future.failed(new WebServiceException(s"Statut HTTP non géré. Statut: ${response.status}. Body : ${response.body}"))
      case _ => Future.successful(response)
    }
}
