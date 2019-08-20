package fr.poleemploi.perspectives.metier.infra.elasticsearch

import fr.poleemploi.perspectives.commun.domain.CodeROME
import fr.poleemploi.perspectives.commun.infra.elasticsearch.EsConfig
import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter
import fr.poleemploi.perspectives.metier.domain.{Metier, SecteurActivite}
import play.api.libs.json.{JsArray, Json}
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferentielMetierElasticsearchAdapter(wsClient: WSClient,
                                            esConfig: EsConfig) extends WSAdapter {

  val baseUrl = s"${esConfig.host}:${esConfig.port}"
  val indexName = "secteurs"
  val docType = "_doc"

  def secteursActivites: Future[List[SecteurActivite]] =
    wsClient
      .url(s"$baseUrl/$indexName/_search")
      .withHttpHeaders(jsonContentType)
      .post(Json.obj(
        "size" -> 100,
        "query" -> Json.obj(
          "match_all" -> Json.obj()
        ),
        "sort" -> Json.arr(
          Json.obj("label" -> "asc")
        )
      ))
      .flatMap(filtreStatutReponse(_))
      .map {
        r =>
          (Json.parse(r.body) \ "hits" \ "hits" \\ "_source").map { jsValue =>
            val d = jsValue.as[SecteurActiviteDocument]
            SecteurActivite(
              code = d.code,
              label = d.label,
              domainesProfessionnels = Nil,
              metiers = d.sousSecteurs.sortBy(_.label).map(s =>
                Metier(
                  codeROME = CodeROME(s.code),
                  label = s.label
                )
              )
            )
          }.toList
      }
}
