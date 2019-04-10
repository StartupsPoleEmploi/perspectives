package fr.poleemploi.perspectives.projections.metier

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.commun.domain.CodeROME

case class MetierRechercheParCodeROMEQuery(codeROME: CodeROME) extends Query[MetierRechercheParCodeROMEQueryResult]

case class MetierRechercheParCodeROMEQueryResult(metier: MetierDTO) extends QueryResult
