package fr.poleemploi.cqrs.projection

/**
  *
  * Sert à interroger les projections
  *
  * @tparam R Le type de QueryResult renvoyé
  */
trait Query[R <: QueryResult]

trait QueryResult
