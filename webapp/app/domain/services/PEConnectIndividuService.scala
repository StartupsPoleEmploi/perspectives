package domain.services

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class UserInfos(peConnectId: String,
                     nom: String,
                     prenom: String,
                     email: String)

object UserInfos {

  // TODO : Infra et pas service du domaine
  implicit val userInfoReads: Reads[UserInfos] = (
    (JsPath \ "sub").read[String] and
    (JsPath \ "family_name").read[String] and
      (JsPath \ "given_name").read[String] and
      (JsPath \ "email").read[String]
    ) (UserInfos.apply _)
}

class PEConnectIndividuService(wsClient: WSClient,
                               url: String) {

  def getUserInfos(accessToken: String): Future[UserInfos] = {
    wsClient
      .url(s"$url")
      .addHttpHeaders(("Authorization", s"Bearer $accessToken"))
      .get()
      .map(response =>
        if (response.status >= 400) {
          throw new RuntimeException(s"Erreur lors de l'appel aux infos utilisateur. Code: ${response.status}. Reponse : ${response.body}")
        } else if (response.status != 200) {
          throw new RuntimeException(s"Statut non géré lors de l'appel aux infos utilisateur. Code: ${response.status}. Reponse : ${response.body}")
        } else {
          val userInfos = response.json.as[UserInfos]
          userInfos.copy(
            nom = userInfos.nom.toLowerCase,
            prenom = userInfos.prenom.toLowerCase,
            email = userInfos.email.toLowerCase
          )
        }
      )
  }

}
