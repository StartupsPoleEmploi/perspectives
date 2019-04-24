package fr.poleemploi.perspectives.projections.metier

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.metier.domain.SecteurActivite

case object SecteursActiviteQuery extends Query[SecteursActiviteQueryResult]

case class SecteursActiviteQueryResult(secteursActivites: List[SecteurActivite]) extends QueryResult
