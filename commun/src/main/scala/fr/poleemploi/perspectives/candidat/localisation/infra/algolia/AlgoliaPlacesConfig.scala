package fr.poleemploi.perspectives.candidat.localisation.infra.algolia

import play.api.libs.json.{Json, Writes}

case class AlgoliaPlacesConfig(appId: String, apiKey: String)

object AlgoliaPlacesConfig {

  implicit val writes: Writes[AlgoliaPlacesConfig] = Json.writes[AlgoliaPlacesConfig]
}
