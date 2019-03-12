package fr.poleemploi.perspectives.metier.infra.ws

import fr.poleemploi.perspectives.commun.domain.CodeROME
import fr.poleemploi.perspectives.metier.domain.Metier
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

class ReferentielMetierWSMapping {

  def buildMetier(response: RomeCardResponse): Metier =
    Metier(
      codeROME = CodeROME(response.romeProfessionCardCode),
      label = response.romeProfessionCardName
    )
}

case class AccessTokenResponse(accessToken: String,
                               tokenType: String,
                               scope: String,
                               expiresIn: Int)

object AccessTokenResponse {

  implicit val accessTokenResponseReads: Reads[AccessTokenResponse] = (
    (JsPath \ "access_token").read[String] and
      (JsPath \ "token_type").read[String] and
      (JsPath \ "scope").read[String] and
      (JsPath \ "expires_in").read[Int]
    ) (AccessTokenResponse.apply _)
}

case class LinksResponse(start: String,
                         next: Option[String])

object LinksResponse {

  implicit val linksResponseReads: Reads[LinksResponse] = (
    (JsPath \ "start").read[String] and
      (JsPath \ "next").readNullable[String]
    ) (LinksResponse.apply _)
}

case class ListeMetiersResponse(records: List[RomeCardResponse],
                                links: LinksResponse,
                                total: Int)

/**
  * On ne peut pas se baser sur l'attribut _links retourné car il renvoit toujours un next,
  * même quand on a dépassé l'offset et qu'il n'y a plus de records retournés
  */
object ListeMetiersResponse {

  implicit val listeMetiersResponseReads: Reads[ListeMetiersResponse] = (
    (JsPath \ "result" \ "records").read[List[RomeCardResponse]] and
      (JsPath \ "result" \ "_links").read[LinksResponse] and
      (JsPath \ "result" \ "total").read[Int]
    ) (ListeMetiersResponse.apply _)
}

case class RomeCardResponse(romeProfessionCardName: String,
                            romeProfessionCardCode: String)

object RomeCardResponse {

  implicit val romeCardResponseReads: Reads[RomeCardResponse] = (
    (JsPath \ "ROME_PROFESSION_CARD_NAME").read[String].map(_.replaceAll("''", "'")) and
      (JsPath \ "ROME_PROFESSION_CARD_CODE").read[String]
    ) (RomeCardResponse.apply _)
}
