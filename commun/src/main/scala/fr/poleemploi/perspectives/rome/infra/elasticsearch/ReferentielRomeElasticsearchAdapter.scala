package fr.poleemploi.perspectives.rome.infra.elasticsearch

import fr.poleemploi.perspectives.commun.infra.elasticsearch.EsConfig
import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter
import fr.poleemploi.perspectives.rome.domain.Appellation
import fr.poleemploi.perspectives.rome.infra.elasticsearch.ReferentielRomeElasticsearchMapping.{docType, indexName}
import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferentielRomeElasticsearchAdapter(wsClient: WSClient,
                                          esConfig: EsConfig,
                                          mapping: ReferentielRomeElasticsearchMapping) extends WSAdapter with Logging {

  val baseUrl = s"${esConfig.host}:${esConfig.port}"

  def indexAppellations(appellations: Seq[Appellation]): Future[Unit] = {
    val bulkContent = appellations.flatMap(buildBulkContent).mkString("\n") + "\n"

    wsClient
      .url(s"$baseUrl/_bulk")
      .withHttpHeaders(jsonContentType)
      .post(bulkContent)
      .flatMap(filtreStatutReponse(_))
      .map(_ => logger.info(s"${appellations.size} appellations ont bien été chargées dans l'index ES $indexName"))
  }

  def appellationsRecherche(query: String): Future[Seq[Appellation]] =
    wsClient
      .url(s"$baseUrl/$indexName/_search")
      .withHttpHeaders(jsonContentType)
      .post(mapping.buildRechercheAppellationsFromQuery(query))
      .flatMap(filtreStatutReponse(_))
      .map { r =>
        (Json.parse(r.body) \ "hits" \ "hits" \\ "_source").map { jsValue =>
          val d = jsValue.as[AppellationDocument]
          Appellation(
            codeROME = d.codeROME,
            codeAppellation = d.codeAppellation,
            label = d.label
          )
        }
      }

  private def buildBulkContent(appellation: Appellation): Seq[String] =
    Seq(
      Json.toJson(BulkIndexDocument(index = BulkIndexMetadataDocument(
        _index = indexName,
        _type = docType,
        _id = appellation.codeAppellation.value
      ))).toString(),
      Json.toJson(appellation).toString()
    )
}
