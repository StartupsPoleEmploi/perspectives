package fr.poleemploi.eventsourcing

trait Aggregate {

  type Id <: AggregateId

  def id: Id

  def version: Int
}
