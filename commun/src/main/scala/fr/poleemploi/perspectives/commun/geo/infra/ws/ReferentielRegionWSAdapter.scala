package fr.poleemploi.perspectives.commun.geo.infra.ws

import fr.poleemploi.perspectives.commun.StringUtils
import fr.poleemploi.perspectives.commun.domain.{Departement, Region}
import fr.poleemploi.perspectives.commun.geo.domain.ReferentielRegion
import fr.poleemploi.perspectives.commun.infra.ws._
import play.api.cache.AsyncCacheApi
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferentielRegionWSAdapter(wsClient: WSClient,
                                 config: ReferentielRegionWSConfig,
                                 mapping: ReferentielRegionWSMapping,
                                 cacheApi: AsyncCacheApi) extends ReferentielRegion with WSAdapter {

  private val cacheKeyRegions = "referentiel.regions"
  private val cacheKeyDepartements = "referentiel.departements"

  override def regions: Future[List[Region]] =
    cacheApi.getOrElseUpdate(cacheKeyRegions)(
      wsClient.url(s"${config.urlApi}/regions?fields=nom,code")
        .withHttpHeaders(("Accept", "application/json"))
        .get()
        .flatMap(filtreStatutReponse(_))
        .map(_.json.as[List[RegionResponse]].map(mapping.buildRegion).sortBy(x => StringUtils.unaccent(x.label)))
    )

  override def departements: Future[List[Departement]] =
    cacheApi.getOrElseUpdate(cacheKeyDepartements)(
      wsClient.url(s"${config.urlApi}/departements?fields=nom,code,codeRegion")
        .withHttpHeaders(("Accept", "application/json"))
        .get()
        .flatMap(filtreStatutReponse(_))
        .map(_.json.as[List[DepartementResponse]].map(mapping.buildDepartement).sortBy(_.label.toUpperCase()))
    )
}
