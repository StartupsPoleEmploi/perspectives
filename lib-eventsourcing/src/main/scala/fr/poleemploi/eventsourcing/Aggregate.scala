package fr.poleemploi.eventsourcing

trait Aggregate {

  type Id <: AggregateId

  def id: Id

  def version: Int
}

trait AggregateState {

  def apply(event: Event): AggregateState
}
