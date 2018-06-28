package fr.poleemploi.eventsourcing

trait Aggregate {

  def id: AggregateId

  def version: Int
}

trait AggregateState {

  def apply(event: Event): AggregateState
}
