package fr.poleemploi.eventsourcing

trait AggregateId {

  def value: String
}
