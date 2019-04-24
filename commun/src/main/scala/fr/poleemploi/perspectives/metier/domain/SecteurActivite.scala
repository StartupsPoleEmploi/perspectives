package fr.poleemploi.perspectives.metier.domain

import fr.poleemploi.perspectives.commun.domain.CodeSecteurActivite
import play.api.libs.json.{Json, Writes}

case class SecteurActivite(code: CodeSecteurActivite,
                           label: String,
                           domainesProfessionnels: List[DomaineProfessionnel],
                           metiers: List[Metier])

object SecteurActivite {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[SecteurActivite] = Json.writes[SecteurActivite]
}
