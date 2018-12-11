package fr.poleemploi.perspectives.commun.infra.ws

import play.api.libs.ws.WSResponse

class WebServiceException(message: String) extends Exception(message)

trait WSAdapter {

  def filtreStatutReponse(response: WSResponse,
                          statutErreur: Int => Boolean = s => s >= 400,
                          statutNonGere: Int => Boolean = s => s != 200): WSResponse = response.status match {
    case s if statutErreur(s) => throw new WebServiceException(s"Erreur lors de l'appel à un web service. Code: ${response.status}. Reponse : ${response.body}")
    case s if statutNonGere(s) => throw new WebServiceException(s"Statut non géré lors de l'appel à un web service. Code: ${response.status}. Reponse : ${response.body}")
    case _ => response
  }

}
