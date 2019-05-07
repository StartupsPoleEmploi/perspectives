package fr.poleemploi.perspectives.projections.conseiller.mrs

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.commun.domain.CodeROME
import play.api.libs.json.{Writes, _}

case object CodeROMEsAvecHabiletesQuery extends Query[CodeROMEsAvecHabiletesQueryResult]

case class CodeROMEsAvecHabiletesQueryResult(codeROMEs: List[CodeROME]) extends QueryResult

object CodeROMEsAvecHabiletesQueryResult {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val writes: Writes[CodeROMEsAvecHabiletesQueryResult] =
    (__ \ "result").write[List[CodeROME]].contramap(_.codeROMEs)
}
