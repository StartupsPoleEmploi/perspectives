package fr.poleemploi.perspectives.projections.metier

import fr.poleemploi.cqrs.projection.{Query, QueryResult}

case object SecteursActiviteQuery extends Query[SecteursActiviteQueryResult]

case class SecteursActiviteQueryResult(secteursActivites: List[SecteurActiviteDTO]) extends QueryResult
