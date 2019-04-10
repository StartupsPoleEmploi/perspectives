package fr.poleemploi.perspectives.projections.metier

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.commun.domain.CodeSecteurActivite

case class SecteurActiviteParCodeQuery(code: CodeSecteurActivite) extends Query[SecteurActiviteParCodeQueryResult]

case class SecteurActiviteParCodeQueryResult(secteurActiviteDTO: SecteurActiviteDTO) extends QueryResult
