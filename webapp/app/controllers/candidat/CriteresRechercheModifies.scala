package controllers.candidat

import fr.poleemploi.perspectives.commun.domain.{CodeROME, RayonRecherche}
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import play.api.libs.json.{Format, Json, Writes}

case class CriteresRechercheModifies(rechercheMetiersEvalues: Boolean,
                                     rechercheAutresMetiers: Boolean,
                                     metiersRecherches: Set[CodeROME],
                                     rayonRecherche: RayonRecherche)

object CriteresRechercheModifies {

  implicit val format: Format[CriteresRechercheModifies] = Json.format[CriteresRechercheModifies]
}
