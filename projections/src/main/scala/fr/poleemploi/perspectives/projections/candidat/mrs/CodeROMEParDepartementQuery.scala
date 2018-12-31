package fr.poleemploi.perspectives.projections.candidat.mrs

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME}
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import play.api.libs.functional.syntax._
import play.api.libs.json.{Writes, _}

case class CodeROMEParDepartementQuery() extends Query[CodeROMEParDepartementQueryResult]

case class CodeROMEParDepartementQueryResult(result: Map[CodeDepartement, List[CodeROME]]) extends QueryResult

object CodeROMEParDepartementQueryResult {

  implicit val writes: Writes[CodeROMEParDepartementQueryResult] =
    (__ \ "result").write[Map[CodeDepartement, List[CodeROME]]].contramap(_.result)
}
