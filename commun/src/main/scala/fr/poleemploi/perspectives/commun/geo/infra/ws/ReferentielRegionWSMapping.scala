package fr.poleemploi.perspectives.commun.geo.infra.ws

import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeRegion, Departement, Region}
import play.api.libs.json.{Json, Reads}

class ReferentielRegionWSMapping {

  def buildRegion(response: RegionResponse): Region =
    Region(
      code = CodeRegion(response.code),
      label = response.nom
    )

  def buildDepartement(response: DepartementResponse): Departement =
    Departement(
      code = CodeDepartement(response.code),
      label = response.nom,
      codeRegion = CodeRegion(response.codeRegion)
    )
}

case class RegionResponse(nom: String,
                          code: String)

object RegionResponse {

  implicit val reads: Reads[RegionResponse] = Json.reads[RegionResponse]
}

case class DepartementResponse(nom: String,
                               code: String,
                               codeRegion: String)

object DepartementResponse {

  implicit val reads: Reads[DepartementResponse] = Json.reads[DepartementResponse]
}