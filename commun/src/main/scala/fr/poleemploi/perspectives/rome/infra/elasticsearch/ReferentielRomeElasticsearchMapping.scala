package fr.poleemploi.perspectives.rome.infra.elasticsearch

import play.api.libs.json.{JsObject, Json}

import scala.concurrent.ExecutionContext.Implicits.global

class ReferentielRomeElasticsearchMapping {

  import ReferentielRomeElasticsearchMapping._

  def buildRechercheAppellationsFromQuery(query: String): JsObject =
    Json.obj(
      "size" -> 10,
      "query" -> Json.obj(
        "match" -> Json.obj(
          label_appellation -> Json.obj(
            "query" -> query,
            "operator" -> "and",
            "fuzziness" -> "AUTO"
          )
        )
      )
    )
}

object ReferentielRomeElasticsearchMapping {
  val indexName = "appellations"
  val docType = "_doc"

  val label_appellation = "label"
}
