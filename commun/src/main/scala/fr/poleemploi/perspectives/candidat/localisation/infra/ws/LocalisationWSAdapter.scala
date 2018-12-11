package fr.poleemploi.perspectives.candidat.localisation.infra.ws

import fr.poleemploi.perspectives.candidat.Adresse
import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.commun.domain.Coordonnees
import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LocalisationWSAdapter(wsClient: WSClient,
                            config: LocalisationWSAdapterConfig,
                            mapping: LocalisationWSMapping) extends LocalisationService with WSAdapter {

  override def localiser(adresse: Adresse): Future[Option[Coordonnees]] =
    wsClient.url(s"${config.urlApi}/search")
      .withQueryStringParameters(
        ("q", s"${adresse.voie}"),
        ("postcode", s"${adresse.codePostal}"),
        ("autocomplete", s"0"),
        ("limit", "1") // Une seul résultat suffit : celui avec le score le plus élevé sera retourné
      )
      .get()
      .map(filtreStatutReponse(_))
      .map(response => {
        val coordonnees = (Json.parse(response.body) \\ "coordinates").headOption.map(json => json.as[GeometryCoordinates])
        coordonnees.map(mapping.buildCoordonnees)
      })
}
