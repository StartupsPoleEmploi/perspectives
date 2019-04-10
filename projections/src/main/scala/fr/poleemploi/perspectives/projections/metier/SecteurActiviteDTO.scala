package fr.poleemploi.perspectives.projections.metier

import fr.poleemploi.perspectives.commun.domain.CodeSecteurActivite
import play.api.libs.json.{Json, Writes}

// FIXME : supprimer au final?
case class SecteurActiviteDTO(code: CodeSecteurActivite,
                              label: String,
                              metiers: List[MetierDTO],
                              domainesProfessionnels: List[DomaineProfessionnelDTO])

object SecteurActiviteDTO {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[SecteurActiviteDTO] = Json.writes[SecteurActiviteDTO]
}
