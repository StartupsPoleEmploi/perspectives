package fr.poleemploi.perspectives.projections.metier

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.commun.domain.CodeSecteurActivite
import fr.poleemploi.perspectives.metier.domain.SecteurActivite

case class SecteurActiviteParCodeQuery(code: CodeSecteurActivite) extends Query[SecteurActiviteParCodeQueryResult]

case class SecteurActiviteParCodeQueryResult(secteurActivite: SecteurActivite) extends QueryResult
